import chisel3._
import chisel3.util._

class Accelerator extends Module {
  val io = IO(new Bundle {
    val start = Input(Bool())
    val done = Output(Bool())

    val address = Output(UInt (16.W))
    val dataRead = Input(UInt (32.W))
    val writeEnable = Output(Bool ())
    val dataWrite = Output(UInt (32.W))

    val x = Output(UInt (16.W))
    val y = Output(UInt (16.W))
    val window = Output(UInt (41.W))
    val state = Output(UInt (2.W))
  })

  //Write here your code
  start := false.B
  address := 0.U
  dataWrite := 0.U
  done := false.B

  io.state = 0.U

  // we could have it as an infinite loop as well per fsmd
  while done === false.B {
    io.state match {
      case 0.U => {
        if (io.start) {
          io.state = 1.U
          io.x = 0
          io.y = 0
        }
      }
      // prefill
      case 1.U => {
        address := io.x + io.y * 20 + 20
        window := pixel
      }
      // readDown
      case 2.U => {
        
      }
      // write
      case 3.U => {
        
      }
      // writeLast
      case 4.U => {
        
      }
      // done
      case 5.U => {
        done := true.B
      }
    }
  }
}
