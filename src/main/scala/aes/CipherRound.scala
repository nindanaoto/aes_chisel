package aes

import chisel3._
import chisel3.util._

// implements AES_Encrypt round transforms
class CipherRound(transform: String, SubBytes_SCD: Boolean = false) extends Module {
  require(transform == "AddRoundKeyOnly" || transform == "NoMixColumns" || transform == "CompleteRound" || transform == "UnRolled" || transform == "UnRolledARK" || transform == "UnRolledNMC")
  val io = IO(new Bundle {
    val input_valid = Input(Bool())
    val state_in = Input(Vec(Params.StateLength, UInt(8.W)))
    val roundKey = Input(Vec(Params.StateLength, UInt(8.W)))
    val state_out = Output(Vec(Params.StateLength, UInt(8.W)))
    val output_valid = Output(Bool())
  })

  // A well defined 'DontCare' or Initialization value

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

  } else if (transform == "NoMixColumns") {

    // Instantiate module objects
    val AddRoundKeyModule = AddRoundKey()
    val SubBytesModule = SubBytes(SubBytes_SCD)
    val ShiftRowsModule = ShiftRows()

    // SubBytes and AddRoundKeyModule roundKey
    SubBytesModule.io.state_in := io.state_in
    AddRoundKeyModule.io.roundKey := io.roundKey
    // ShiftRows
    ShiftRowsModule.io.state_in := SubBytesModule.io.state_out
    // AddRoundKey
    AddRoundKeyModule.io.state_in := ShiftRowsModule.io.state_out

    // output
    io.state_out := ShiftRegister(AddRoundKeyModule.io.state_out, 1)
    io.output_valid := ShiftRegister(io.input_valid, 1)

  } else if (transform == "CompleteRound") {

    // Instantiate module objects
    val AddRoundKeyModule = AddRoundKey()
    val SubBytesModule = SubBytes(SubBytes_SCD)
    val ShiftRowsModule = ShiftRows()
    val MixColumnsModule = MixColumns()

    // SubBytes and AddRoundKeyModule roundKey
    SubBytesModule.io.state_in := io.state_in
    AddRoundKeyModule.io.roundKey := io.roundKey
    // ShiftRows
    ShiftRowsModule.io.state_in := SubBytesModule.io.state_out
    // MixColumns
    MixColumnsModule.io.state_in := ShiftRowsModule.io.state_out
    // AddRoundKey
    AddRoundKeyModule.io.state_in := MixColumnsModule.io.state_out

    // output
    io.state_out := ShiftRegister(AddRoundKeyModule.io.state_out, 1)
    io.output_valid := ShiftRegister(io.input_valid, 1)
  }else if (transform == "UnRolled") {

    // Instantiate module objects
    val AddRoundKeyModule = AddRoundKey()
    val SubBytesModule = SubBytes(SubBytes_SCD)
    val ShiftRowsModule = ShiftRows()
    val MixColumnsModule = MixColumns()

    // SubBytes and AddRoundKeyModule roundKey
      SubBytesModule.io.state_in := io.state_in
      AddRoundKeyModule.io.roundKey := io.roundKey
    // ShiftRows
    ShiftRowsModule.io.state_in := SubBytesModule.io.state_out
    // MixColumns
    MixColumnsModule.io.state_in := ShiftRowsModule.io.state_out
    // AddRoundKey
    AddRoundKeyModule.io.state_in := MixColumnsModule.io.state_out

    // output
    io.state_out := AddRoundKeyModule.io.state_out
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

  } else if (transform == "UnRolledNMC") {

    // Instantiate module objects
    val AddRoundKeyModule = AddRoundKey()
    val SubBytesModule = SubBytes(SubBytes_SCD)
    val ShiftRowsModule = ShiftRows()

    // SubBytes and AddRoundKeyModule roundKey
    SubBytesModule.io.state_in := io.state_in
    AddRoundKeyModule.io.roundKey := io.roundKey
    // ShiftRows
    ShiftRowsModule.io.state_in := SubBytesModule.io.state_out
    // AddRoundKey
    AddRoundKeyModule.io.state_in := ShiftRowsModule.io.state_out

    // output
    io.state_out := ShiftRegister(AddRoundKeyModule.io.state_out, 1)
    io.output_valid := ShiftRegister(io.input_valid, 1)

  } 

}