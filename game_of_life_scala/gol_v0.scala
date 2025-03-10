import scala.swing._
import scala.swing.event._
import java.awt.{Color, Dimension, Graphics2D}
import javax.swing.Timer

abstract class Cell {
  def isAlive: Boolean
  def nextState(neighbors: Int): Cell
}

case object LiveCell extends Cell {
  def isAlive = true
  def nextState(neighbors: Int): Cell =
    if (neighbors == 2 || neighbors == 3) LiveCell else DeadCell
}

case object DeadCell extends Cell {
  def isAlive = false
  def nextState(neighbors: Int): Cell =
    if (neighbors == 3) LiveCell else DeadCell
}

class GameOfLife(rows: Int, cols: Int, cellSize: Int = 20) extends Panel {
  preferredSize = new Dimension(cols * cellSize, rows * cellSize)

  private var grid = Array.fill[Cell](rows, cols)(DeadCell)

  private var hoverCell: Option[(Int, Int)] = None

  listenTo(mouse.clicks, mouse.moves)

  reactions += {
    case MousePressed(_, point, _, _, _) =>
      val (row, col) = (point.y / cellSize, point.x / cellSize)
      setCellAlive(row, col)
      repaint()

    case MouseMoved(_, point, _) =>
      hoverCell = Some((point.y / cellSize, point.x / cellSize))
      repaint()

    case MouseExited(_, _, _) =>
      hoverCell = None
      repaint()
  }

  def setCellAlive(row: Int, col: Int): Unit =
    if (row >= 0 && row < rows && col >= 0 && col < cols)
      grid(row)(col) = LiveCell

  def countNeighbors(row: Int, col: Int): Int =
    (for {
      dr <- (-1).to(1)
      dc <- (-1).to(1)
      if !(dr == 0 && dc == 0)
      r = row + dr
      c = col + dc
      if r >= 0 && r < rows && c >= 0 && c < cols
      if grid(r)(c).isAlive
    } yield 1).sum

  def nextGeneration(): Unit = {
    grid = Array.tabulate(rows, cols)((r, c) => grid(r)(c).nextState(countNeighbors(r, c)))
    repaint()
  }

  override def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    for (r <- 0.until(rows); c <- 0.until(cols)) {
      g.setColor(if (grid(r)(c).isAlive) Color.BLACK else Color.WHITE)
      g.fillRect(c * cellSize, r * cellSize, cellSize, cellSize)
      
      g.setColor(Color.LIGHT_GRAY)
      g.drawRect(c * cellSize, r * cellSize, cellSize, cellSize)
    }

    // Draw hover effect
    hoverCell.foreach { case (r, c) =>
      g.setColor(Color.RED)
      g.drawRect(c * cellSize, r * cellSize, cellSize, cellSize)
    }
  }

  // Predefined patterns
  def addGlider(x: Int, y: Int): Unit = {
    setCellAlive(x, y + 1)
    setCellAlive(x + 1, y + 2)
    setCellAlive(x + 2, y)
    setCellAlive(x + 2, y + 1)
    setCellAlive(x + 2, y + 2)
  }

  def addSmallExploder(x: Int, y: Int): Unit = {
    setCellAlive(x, y + 1)
    setCellAlive(x + 1, y)
    setCellAlive(x + 1, y + 1)
    setCellAlive(x + 1, y + 2)
    setCellAlive(x + 2, y + 1)
  }

  def addGliderGun(x: Int, y: Int): Unit = {
    // Can add more patterns similarly
  }
}

object GameOfLifeApp extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "Conway's Game of Life (Scala)"
    val game = new GameOfLife(30, 30)

    game.addSmallExploder(10, 10)
    game.addGlider(1, 1)

    contents = game

    new Timer(200, _ => game.nextGeneration()).start()
  }
}