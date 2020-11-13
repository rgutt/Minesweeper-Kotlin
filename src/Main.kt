package minesweeper
import java.util.*

fun nonRepeatingIntegerList(start: Int = 0, end: Int, n: Int) =  (start until end).toList().shuffled().take(n)



class Pseudo2DArray(private val width: Int, height: Int) {
    val size: Int = width * height
    private  val pseudo2DArray: IntArray  = IntArray(size)
    operator fun get(i: Int, j: Int) = pseudo2DArray[i + j * width]
    operator fun get(i: Int) = pseudo2DArray[i]
    operator fun set(i: Int, j: Int, value: Int) {
        pseudo2DArray[i + j * width] = value
    }
    operator fun set(i: Int, value: Int) {
        pseudo2DArray[i] = value
    }
    fun contains(i: Int) = pseudo2DArray.contains(i)

}

class MineArray(private val width: Int, private val height: Int) {
    private val length: Int = width * height
    private val mineField = Pseudo2DArray(width, height)  // -2 mark, -1 mine, 0 empty, 1-8 neighbours, -3 free
    private val visibleMineField = Pseudo2DArray(width, height)
    private var indexOfMine: List<Int> =  listOf<Int>()
    private var neighborsList: ArrayList<List<Int>> = arrayListOf()
    private var result: Int = 0 // -2 ended with operator "exit" -1 claimed free on mine 0-n number of steps

    init {
        initializeNeighborsList()
    }

    private fun initializeNeighborsList() {
        for (k in 0 until length) {
            val i = k % width
            val j = (k / width) % height

            val iList = (i-1..i+1).toList().filter { it in 0 until width  }
            val jList = (j-1..j+1).toList().filter { it in 0 until height }

            val kList = mutableListOf<Int>()

            for (ii in iList) {
                for (jj in jList) {
                    kList.add(ii + jj * width)
                }
            }
            neighborsList.add(kList)
        }
    }

    fun set(nMines: Int) {
        if (nMines < length) {
            indexOfMine = nonRepeatingIntegerList(end = length, n = nMines)
            indexOfMine.forEach {
                    i -> neighborsList[i].forEach{
                    j -> mineField[j] += 1
            }
                mineField[i] = -1
            }
        }
    }

    override fun toString(): String {
        var output = " |123456789|\n"
        output += "—|—————————|\n"
        for (j in 0 until height) {
            output += "${j + 1}|"
            for (i in 0 until width) {
                when (visibleMineField[i,j]) {
                    -4 -> output += 'X'
                    -3 -> output += '/'
                    -2 -> output += '*'
                    -1 -> output += 'X'
                    0 -> output += '.'
                    else -> output += visibleMineField[i,j].toString()
                }
            }
            output += "|\n"
        }
        output += "—|—————————|\n"
        return output
    }

    fun setOption(i: Int, j: Int, op: String) {
        result += 1
        return when (op) {
            "mine" -> {
                markMine(i, j)
            }
            "free" -> {
                claimFreeCell(i,j)
            }
            else ->  {
                result = -2
            }
        }
    }

    private fun markMine(i: Int, j: Int) {
        visibleMineField[i, j] = when (visibleMineField[i, j]) {
            0 -> -2
            -1 -> -2
            -2 -> 0
            -3 -> -3
            else -> mineField[i,j]
        }
    }

    private fun exploreNeighbors(i: Int,j: Int) {
        val ii: Int = get1DId(i, j)
        if (mineField[i, j] == 0 && visibleMineField[i, j] == 0) {
            visibleMineField[i, j] = -3
            for (n in neighborsList[ii]) {
                exploreNeighbors(get2DId(n)[0], get2DId(n)[1])
            }
        }
        if (mineField[i, j] > 0)
            visibleMineField[i,j] = mineField[i,j]

    }

    private fun showAllMines() {
        for (i in indexOfMine)
            visibleMineField[i] = -1
    }

    private fun claimFreeCell(i: Int, j: Int) {
        visibleMineField[i,j] = mineField[i,j]
        return if (indexOfMine.contains( i + j * width)) {
            showAllMines()
            result = -1
        } else {
            if ( mineField[i,j] == 0 ) {
                exploreNeighbors(i, j)
            } else
                visibleMineField[i,j] = mineField[i,j]
        }
    }

    fun allFound(): Boolean {// -2 mark, -1 mine, 0 empty, 1-8 neighbours, -3 free

        var allMarked = true
        var allExplored = true

        for (i in indexOfMine) {
            if (visibleMineField[i] != -2) {
                allMarked = false
            }
        }

        for (i in 0 until visibleMineField.size) {
            if (visibleMineField[i] == -3 && visibleMineField[i] != mineField[i]) {
                allExplored = false
            }
        }
        return ( allMarked || allExplored )
    }

    fun result() = result

    private fun get2DId(i: Int) = listOf<Int>(i % width,(i / width) % height)
    private fun get1DId(i: Int, j: Int) = i + j * width
}


object Minefield {
    private const val WIDTH: Int = 9
    private const val HEIGHT: Int = 9
    private val mineArray = MineArray(WIDTH, HEIGHT)

    private fun printMinefield() = print(mineArray)

    private fun selectOption(i: Int, j: Int, op: String){
        mineArray.setOption(i,j,op)
    }


    private fun setMinefield(nMines: Int) = mineArray.set(nMines)

    private val input = Scanner(System.`in`)

    fun run() {
        println("How many mine do you want on the field?")
        setMinefield(input.nextInt())
        printMinefield()

        do {
            println("Set/unset mines marks or claim a cell as free: ")
            val x = input.nextInt() - 1
            val y = input.nextInt() - 1
            val op = input.next()
            selectOption(x,y,op)
            printMinefield()
     //       print(!mineArray.allFound())
        } while ( !mineArray.allFound() && mineArray.result() >= 0 )


        when(mineArray.result()) {
            -2 -> println("You exited the game before finishing it!")
            -1 -> println("You stepped on a mine and failed!")
            else -> println("Congratulations! You found all the mines! in ${mineArray.result()} steps!")
        }

    }
}

fun main() {
    Minefield.run()
}