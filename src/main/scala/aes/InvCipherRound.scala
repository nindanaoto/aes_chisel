package aes

import chisel3._
import chisel3.util._

// implements AES_Decrypt round transforms
class InvCipherRound(transform: String, InvSubBytes_SCD: Boolean) extends Module {
  require(transform == "AddRoundKeyOnly" || transform == "NoInvMixColumns" || transform == "CompleteRound" || transform == "UnRolled" || transform == "UnRolledARK" || transform == "UnRolledNIMC")
  val io = IO(new Bundle {
    val input_valid = Input(Bool())
    val state_in = Input(Vec(Params.StateLength, UInt(8.W)))
    val roundKey = Input(Vec(Params.StateLength, UInt(8.W)))
    val state_out = Output(Vec(Params.StateLength, UInt(8.W)))
    val output_valid = Output(Bool())
  })

  // Transform sequences
  if (transform == "AddRoundKeyOnly") {

    // Instantiate module objects
    val AddRoundKeyModule = AddRoundKey()

    // AddRoundKey
    AddRoundKeyModule.io.state_in := io.state_in
    AddRoundKeyModule.io.roundKey := io.roundKey

    // output
    io.state_out := ShiftRegister(AddRoundKeyModule.io.state_out, 1)
    io.output_valid := ShiftRegister(io.input_valid, 1)

  } else if (transform == "NoInvMixColumns") {

    // Instantiate module objects
    val AddRoundKeyModule = AddRoundKey()
    val InvSubBytesModule = InvSubBytes(InvSubBytes_SCD)
    val InvShiftRowsModule = InvShiftRows()

    // InvShiftRows and AddRoundKeyModule roundKey
    InvShiftRowsModule.io.state_in := io.state_in
    AddRoundKeyModule.io.roundKey := io.roundKey
    // InvSubBytes
    InvSubBytesModule.io.state_in := InvShiftRowsModule.io.state_out
    // AddRoundKey
    AddRoundKeyModule.io.state_in := InvSubBytesModule.io.state_out

    // output
    io.state_out := ShiftRegister(AddRoundKeyModule.io.state_out, 1)
    io.output_valid := ShiftRegister(io.input_valid, 1)

  } else if (transform == "CompleteRound") {

    // Instantiate module objects
    val AddRoundKeyModule = AddRoundKey()
    val InvSubBytesModule = InvSubBytes(InvSubBytes_SCD)
    val InvShiftRowsModule = InvShiftRows()
    val InvMixColumnsModule = InvMixColumns()

    // InvShiftRows and AddRoundKeyModule roundKey
    InvShiftRowsModule.io.state_in := io.state_in
    AddRoundKeyModule.io.roundKey := io.roundKey
    // InvSubBytes
    InvSubBytesModule.io.state_in := InvShiftRowsModule.io.state_out
    // AddRoundKey
    AddRoundKeyModule.io.state_in := InvSubBytesModule.io.state_out
    // InvMixColumns
    InvMixColumnsModule.io.state_in := AddRoundKeyModule.io.state_out

    // output
    io.state_out := ShiftRegister(InvMixColumnsModule.io.state_out, 1)
    io.output_valid := ShiftRegister(io.input_valid, 1)

  } else if (transform == "UnRolled") {

    // Instantiate module objects
    val AddRoundKeyModule = AddRoundKey()
    val InvSubBytesModule = InvSubBytes(InvSubBytes_SCD)
    val InvShiftRowsModule = InvShiftRows()
    val InvMixColumnsModule = InvMixColumns()

    // InvShiftRows and AddRoundKeyModule roundKey
      InvShiftRowsModule.io.state_in := io.state_in
      AddRoundKeyModule.io.roundKey := io.roundKey
    // InvSubBytes
    InvSubBytesModule.io.state_in := InvShiftRowsModule.io.state_out
    // AddRoundKey
    AddRoundKeyModule.io.state_in := InvSubBytesModule.io.state_out
    // InvMixColumns
    InvMixColumnsModule.io.state_in := AddRoundKeyModule.io.state_out

    // output
    io.state_out := InvMixColumnsModule.io.state_out
    io.output_valid := io.input_valid

  }else if (transform == "UnRolledARK") {

    // Instantiate module objects
    val AddRoundKeyModule = AddRoundKey()

    // AddRoundKey
      AddRoundKeyModule.io.state_in := io.state_in
      AddRoundKeyModule.io.roundKey := io.roundKey

    // output
    io.state_out := AddRoundKeyModule.io.state_out
    io.output_valid := io.input_valid

  } else if (transform == "UnRolledNIMC") {

    // Instantiate module objects
    val AddRoundKeyModule = AddRoundKey()
    val InvSubBytesModule = InvSubBytes(InvSubBytes_SCD)
    val InvShiftRowsModule = InvShiftRows()

    // InvShiftRows and AddRoundKeyModule roundKey
      InvShiftRowsModule.io.state_in := io.state_in
      AddRoundKeyModule.io.roundKey := io.roundKey
    // InvSubBytes
    InvSubBytesModule.io.state_in := InvShiftRowsModule.io.state_out
    // AddRoundKey
    AddRoundKeyModule.io.state_in := InvSubBytesModule.io.state_out

    // output
    io.state_out := AddRoundKeyModule.io.state_out
    io.output_valid := io.input_valid

  } 

}