
import scala.collection.mutable.ArrayBuffer

object ProductUtil {

  import TestLucene.Product
  import java.awt.AWTException
  import java.io.IOException


  @throws[IOException]
  @throws[AWTException]
  def main(args: Array[String]): Unit = {
    val fileName = "140k_products.txt"
    val products = file2list(fileName)
    println(products.length)
  }

  @throws[IOException]
  def file2list(fileName: String): Array[Product] = {
    import scala.io.Source
    val lines = Source.fromFile(fileName).getLines()
    val products = ArrayBuffer[Product]()
    lines.foreach { line =>
      val p = line2product(line)
      products.append(p)
    }
    products.toArray
  }

  private def line2product(line: String): Product = {
    val fields = line.split(",")
    Product(fields(0).toInt, fields(1), fields(2), fields(3).toFloat, fields(4), fields(5))
  }
}
