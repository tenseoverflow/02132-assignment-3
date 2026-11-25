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

  })
  val x = RegInit(0.U(16.W))
  val y = RegInit(0.U(16.W))
  val window = RegInit(VecInit(Seq.fill(41)(0.U(1.W))))
  val state = RegInit(0.U(3.W))

  //Write here your code
  io.address := 0.U
  io.dataWrite := 0.U
  io.writeEnable := false.B
  io.done := false.B

    switch(state) {
      is(0.U)  {
        when (io.start) {
          state := 1.U
          x := 0.U
          y := 0.U
        }
      }
      // prefill
      is(1.U)  {
        io.address := x
        for (i <- 0 until 40) {
          window(i) := window(i + 1)
        }
        window(40) := io.dataRead =/= 0.U

        when (x < 19.U) {
          x := x + 1.U
        } .otherwise {
          x := 0.U
          state := 2.U
        }
      }
      // readDown
      is(2.U)  {
        io.address := x + y * 20.U + 20.U
        for (i <- 0 until 40) {
          window(i) := window(i + 1)
        }
        window(40) := io.dataRead =/= 0.U
        state := 3.U
      }
      // write
      is(3.U)  {
        io.address := x + y * 20.U + 400.U
        io.writeEnable := true.B
        
        when (x === 0.U || x === 19.U || y === 0.U || y === 19.U) {
          io.dataWrite := 0.U
        } .otherwise {
          when ((window(0) === 1.U) && (window(19) === 1.U) && (window(20) === 1.U) && (window(21) === 1.U) && (window(40) === 1.U)) {
            io.dataWrite := 255.U
          } .otherwise {
            io.dataWrite := 0.U
          }
        }

        when (x < 19.U) {
          x := x + 1.U
          state := 2.U
        } .elsewhen ((y =/= 18.U) && (x === 19.U)) {
          x := 0.U
          y := y + 1.U
          state := 2.U
        } .elsewhen ((y === 18.U) && (x === 19.U)) {
          y := 0.U
          x := 0.U
          state := 4.U
        }
      }
      // writeLast
      is(4.U)  {
        io.address := x + 780.U
        io.writeEnable := true.B
        io.dataWrite := 0.U

        when (x < 19.U) {
          x := x + 1.U
        } .otherwise {
          state := 5.U
        }
      }
      // done
      is(5.U) {
        io.done := true.B
      }
    }
}
