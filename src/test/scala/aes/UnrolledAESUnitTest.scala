package aes

import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._
import chisel3._
import chiseltest.VerilatorBackendAnnotation

// Run test with:
// sbt 'testOnly aes.UnrolledAESTester'
// sbt 'testOnly aes.UnrolledAESTester -- -z "using verilator"'
// sbt 'testOnly aes.UnrolledAESTester -- -z "using firrtl"'
// sbt 'testOnly aes.UnrolledAESTester -- -z verbose'
// sbt 'testOnly aes.UnrolledAESTester -- -z vcd'

class UnrolledAESTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Testers2"

  // private val SubBytes_SCD = false
  // private val InvSubBytes_SCD = false
  // private val Nk = 4 // 4, 6, 8 [32-bit words] columns in cipher key
  
  it should "test Unrolled AES on fixed data" in {
        test(new UnrolledAES(4,false,false)).withAnnotations(Seq(VerilatorBackendAnnotation)){c =>
              printf("\nStarting AES cipher mode, sending plaintext\n")
              // val input_text = Array(0x32, 0x43, 0xf6, 0xa8, 0x88, 0x5a, 0x30, 0x8d, 0x31, 0x31, 0x98, 0xa2, 0xe0, 0x37, 0x07, 0x34)

              // val roundKey128 = Array(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f)

              // val roundKey192 = Array(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17)

              // val roundKey256 = Array(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f)

              // val roundKey = Nk match {
              //   case 4 => roundKey128
              //   case 6 => roundKey192
              //   case 8 => roundKey256
              // }
              // c.io.AES_mode.poke(2.U) // cipher
              // // send the plaintext
              // for (i <- 0 until Params.StateLength) {
              //   c.io.input_text(i).poke(input_text(i).U)
              // }
              // c.clock.step(1)

              // val state_e128 = Array(0x89, 0xed, 0x5e, 0x6a, 0x05, 0xca, 0x76, 0x33, 0x81, 0x35, 0x08, 0x5f, 0xe2, 0x1c, 0x40, 0xbd)
              // val state_e192 = Array(0xbc, 0x3a, 0xaa, 0xb5, 0xd9, 0x7b, 0xaa, 0x7b, 0x32, 0x5d, 0x7b, 0x8f, 0x69, 0xcd, 0x7c, 0xa8)
              // val state_e256 = Array(0x9a, 0x19, 0x88, 0x30, 0xff, 0x9a, 0x4e, 0x39, 0xec, 0x15, 0x01, 0x54, 0x7d, 0x4a, 0x6b, 0x1b)

              // val state_e = Nk match {
              //   case 4 => state_e128
              //   case 6 => state_e192
              //   case 8 => state_e256
              // }

              // printf("\nInspecting cipher output\n")
              // // verify aes cipher output
              // for (i <- 0 until Params.StateLength)
              //   c.io.output_text(i).expect(state_e(i).U)

              // // store cipher output
              // val cipher_output = c.io.output_text.peek()

              // printf("\nStarting AES inverse cipher mode, sending ciphertext\n")
              // c.io.AES_mode.poke(3.U) // inverse cipher
              // // send the ciphertext
              // for (i <- 0 until Params.StateLength) {
              //   c.io.input_text(i).poke(cipher_output(i)) // same as state_e(i)
              // }
              // c.clock.step(1)

              // printf("\nInspecting inverse cipher output\n")
              // //  verify aes inverse cipher output
              // for (i <- 0 until Params.StateLength)
              //   c.io.output_text(i).expect(input_text(i).U)
        }
    }

}
