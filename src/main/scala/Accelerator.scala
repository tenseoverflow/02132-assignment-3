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

    val x = Reg(UInt (16.W))
    val y = Reg(UInt (16.W))
    val window = Reg(UInt (41.W))
    val state = Reg(UInt (2.W))
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
        io.address := io.x
        io.window := dataRead

        .when (io.x < 19) {
          io.x := io.x + 1.U
        } .otherwise {
          io.x := 0.U
          io.state = 2.U
        }
      }
      // readDown
      case 2.U => {
        io.address := io.x + io.y * 20 + 20
        io.window := dataRead
        io.state = 3.U
      }
      // write
      case 3.U => {
        io.address := io.x + 20 * io.y + 400
        io.writeEnable := true.B
        
        .when (x == 0 || x == 19 || y == 0) {
          io.dataWrite := 0.U
        } .otherwise {
          .when (io.window(0) && io.window(19) && io.window(20) && io.window(21) && io.window(40)) {
            io.dataWrite := 1.U
          } .otherwise {
            io.dataWrite := 0.U
          }
        }

        .when (io.x < 19) {
          io.x := io.x + 1.U
          io.state = 1.U
        } .elsewhen (io.y != 18 && io.x == 19) {
          io.x := 0.U
          io.y := io.y + 1.U
        } .elsewhen (io.y == 18 && io.x == 19) {
          io.y := 0.U
          io.x := 0.U
          io.state = 4.U
        }
      }
      // writeLast
      case 4.U => {
        io.address := io.x + 780
        io.writeEnable := true.B
        io.dataWrite := 0.U

        .when (io.x < 19) {
          io.x := io.x + 1.U
        } .otherwise {
          io.state = 5.U
        }
      }
      // done
      case 5.U => {
        done := true.B
      }
    }
  }
}
