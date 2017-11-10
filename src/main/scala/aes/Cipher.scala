package aes

import chisel3._

class Cipher extends Module {
  val io = IO(new Bundle {
    val plaintext = Input(Vec(16, UInt(8.W)))
    val expandedKey = Input(Vec(11, Vec(16, UInt(8.W))))
    val state_out = Output(Vec(16, UInt(8.W)))
  })

  val state = Reg(init = io.plaintext)//Vec(16, UInt(8.W)))
  val counter = Reg(init = 0.U)

  // Instantiate module objects
  val AddRoundKeyModule = AddRoundKey()
  val SubBytesModule = SubBytes()
  val ShiftRowsModule = ShiftRows()
  val MixColumnsModule = MixColumns()

  // Whitening with round key
  AddRoundKeyModule.io.state_in := io.plaintext
  AddRoundKeyModule.io.roundKey := io.expandedKey(0)
  state := AddRoundKeyModule.io.state_out

  // Rounds
  for (i <- 1 to 10) {
    SubBytesModule.io.state_in := state

    ShiftRowsModule.io.state_in := SubBytesModule.io.state_out
    // Skip MixColumns in final round
    if (i != 10) {
      MixColumnsModule.io.state_in := ShiftRowsModule.io.state_out

      AddRoundKeyModule.io.state_in := MixColumnsModule.io.state_out
      AddRoundKeyModule.io.roundKey := io.expandedKey(i)
      state := AddRoundKeyModule.io.state_out
    } else {
      AddRoundKeyModule.io.state_in := ShiftRowsModule.io.state_out
      AddRoundKeyModule.io.roundKey := io.expandedKey(i)
      state := AddRoundKeyModule.io.state_out
    }
  }
  io.state_out := state
}