package aes

import chisel3._
import chisel3.util._

// implements wrapper for unrolled AES cipher and inverse cipher
// change expandedKeyMemType= ROM, Mem, SyncReadMem
// change unrolled=[1..Nrplus1] for unroll depth
class UnrolledAES(Nk: Int, SubBytes_SCD: Boolean, InvSubBytes_SCD: Boolean) extends Module {
  require(Nk == 4 || Nk == 6 || Nk == 8)
  val KeyLength: Int = Nk * Params.rows
  val Nr: Int = Nk + 6 // 10, 12, 14 rounds
  val Nrplus1: Int = Nr + 1 // 10+1, 12+1, 14+1

  val io = IO(new Bundle {
    val AES_mode = Input(UInt(2.W)) //  0=00=off, 1=01=expanded key update, 2=10=cipher, 3=11=inverse cipher
    //
    val input_text = Input(UInt((Params.StateLength*8).W)) // plaintext or ciphertext
    //
    val key = Input(UInt(((Nk*Params.StateLength*8).W))) // key
    //
    val output_text = Output(UInt((Params.StateLength*8).W)) // ciphertext or plaintext
  })

  // Declare instances and array of Cipher and Inverse Cipher Rounds
  val CipherRoundARK = Module(new CipherRound("UnRolledARK", SubBytes_SCD))
  val CipherRounds = Array.fill(Nr - 1) {
    Module(new CipherRound("UnRolled", SubBytes_SCD)).io
  }
  val CipherRoundNMC = Module(new CipherRound("UnRolledNMC", SubBytes_SCD))

  val InvCipherRoundARK = Module(new InvCipherRound("UnRolledARK", InvSubBytes_SCD))
  val InvCipherRounds = Array.fill(Nr - 1) {
    Module(new InvCipherRound("UnRolled", InvSubBytes_SCD)).io
  }
  val InvCipherRoundNMC = Module(new InvCipherRound("UnRolledNIMC", InvSubBytes_SCD))

  // A roundKey is Params.StateLength bytes, and 1+(10/12/14) (< EKDepth) of them are needed
  // Mem = combinational/asynchronous-read, sequential/synchronous-write = register banks
  // Create a asynchronous-read, synchronous-write memory block big enough for any key length
  //  val expandedKeyARMem = Mem(EKDepth, Vec(Params.StateLength, UInt(8.W)))

  // SyncReadMem = sequential/synchronous-read, sequential/synchronous-write = SRAMs
  // Create a synchronous-read, synchronous-write memory block big enough for any key length
  //  val expandedKeySRMem = SyncReadMem(EKDepth, Vec(Params.StateLength, UInt(8.W)))

  //
  val expandedKeys = Wire(Vec(Nrplus1, Vec(Params.StateLength, UInt(8.W))))
  val keyexpansion = Module(new KeyExpansion(Nk))
  for(i <- 0 until Nk){
    for(j <- 0 until Params.StateLength){
      val index = i * Params.StateLength + j
      keyexpansion.io.key(i)(j) := io.key(8*(index+1)-1,8*index)
    }
  }
  expandedKeys := keyexpansion.io.roundKeys

  //// Wire Cipher modules together

  // Cipher ARK round
  when(io.AES_mode === 2.U) { // cipher mode
    CipherRoundARK.io.input_valid := true.B
    for(i <- 0 until Params.StateLength){
      CipherRoundARK.io.state_in(i) := io.input_text(8*(i+1)-1,8*i)
    }
    CipherRoundARK.io.roundKey := expandedKeys(0)
  }.otherwise {
    CipherRoundARK.io.input_valid := false.B
    CipherRoundARK.io.state_in := DontCare
    CipherRoundARK.io.roundKey := DontCare
  }

  // Cipher Nr-1 rounds
  for (i <- 0 until (Nr - 1)) yield {
    if (i == 0) {
      CipherRounds(i).input_valid := CipherRoundARK.io.output_valid
      CipherRounds(i).state_in := CipherRoundARK.io.state_out
    }
    else {
      CipherRounds(i).input_valid := CipherRounds(i - 1).output_valid
      CipherRounds(i).state_in := CipherRounds(i - 1).state_out
    }
    CipherRounds(i).roundKey := expandedKeys(i + 1)
  }

  // Cipher last round
  CipherRoundNMC.io.input_valid := CipherRounds(Nr - 1 - 1).output_valid
  CipherRoundNMC.io.state_in := CipherRounds(Nr - 1 - 1).state_out
  CipherRoundNMC.io.roundKey := expandedKeys(Nr)

  //// Wire Inverse Cipher modules together

  // InvCipher ARK round
  when(io.AES_mode === 3.U) { // cipher mode
    InvCipherRoundARK.io.input_valid := true.B
    for(i <- 0 until Params.StateLength){
      InvCipherRoundARK.io.state_in(i) := io.input_text(8*(i+1)-1,8*i)
    }
    InvCipherRoundARK.io.roundKey := expandedKeys(Nr)
  }.otherwise {
    InvCipherRoundARK.io.input_valid := false.B
    InvCipherRoundARK.io.state_in := DontCare
    InvCipherRoundARK.io.roundKey := DontCare
  }

  // Cipher Nr-1 rounds
  for (i <- 0 until (Nr - 1)) yield {
    if (i == 0) {
      InvCipherRounds(i).input_valid := InvCipherRoundARK.io.output_valid
      InvCipherRounds(i).state_in := InvCipherRoundARK.io.state_out
    }
    else {
      InvCipherRounds(i).input_valid := InvCipherRounds(i - 1).output_valid
      InvCipherRounds(i).state_in := InvCipherRounds(i - 1).state_out
    }
    InvCipherRounds(i).roundKey := expandedKeys(Nr - i - 1)
  }

  // Cipher last round
  InvCipherRoundNMC.io.input_valid := InvCipherRounds(Nr - 1 - 1).output_valid
  InvCipherRoundNMC.io.state_in := InvCipherRounds(Nr - 1 - 1).state_out
  InvCipherRoundNMC.io.roundKey := expandedKeys(0)

  //  io.output_text := CipherRoundNMC.io.state_out //Mux((io.AES_mode === 2.U), InvCipherRoundNMC.io.state_out, CipherRoundNMC.io.state_out)
  io.output_text := Cat(Mux(io.AES_mode === 2.U, CipherRoundNMC.io.state_out, InvCipherRoundNMC.io.state_out).reverse)
}

class UnrolledCipher(Nk: Int, SubBytes_SCD: Boolean, InvSubBytes_SCD: Boolean) extends Module {
  val io = IO(new Bundle {
    val input_text = Input(UInt((Params.StateLength*8).W)) // plaintext
    //
    val key = Input(UInt(((Nk*Params.StateLength*8).W))) // key
    //
    val output_text = Output(UInt((Params.StateLength*8).W)) // ciphertext
  })
  val AES = Module(new UnrolledAES(Nk,SubBytes_SCD,InvSubBytes_SCD))
  AES.io.AES_mode := 2.U
  AES.io.input_text := io.input_text
  AES.io.key := io.key
  io.output_text := AES.io.output_text
}


class UnrolledInvCipher(Nk: Int, SubBytes_SCD: Boolean, InvSubBytes_SCD: Boolean) extends Module {
  val io = IO(new Bundle {
    val input_text = Input(UInt((Params.StateLength*8).W)) // ciphertext
    //
    val key = Input(UInt(((Nk*Params.StateLength*8).W))) // key
    //
    val output_text = Output(UInt((Params.StateLength*8).W)) // plaintext
  })
  val AES = Module(new UnrolledAES(Nk,SubBytes_SCD,InvSubBytes_SCD))
  AES.io.AES_mode := 3.U
  AES.io.input_text := io.input_text
  AES.io.key := io.key
  io.output_text := AES.io.output_text
}

object UnrolledAESTop extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new UnrolledAES(4,false,false))
}

object UnrolledCipherTop extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new UnrolledCipher(4,false,false))
}

object UnrolledInvCipherTop extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new UnrolledInvCipher(4,false,false))
}