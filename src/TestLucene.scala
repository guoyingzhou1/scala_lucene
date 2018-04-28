
import org.apache.lucene.document.Document
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.highlight.Highlighter
import org.apache.lucene.search.highlight.QueryScorer
import org.apache.lucene.search.highlight.SimpleHTMLFormatter
import org.apache.lucene.store.{Directory, FSDirectory, RAMDirectory, SimpleFSDirectory}
import org.wltea.analyzer.lucene.IKAnalyzer

import scala.collection.JavaConversions._
import java.io.{BufferedReader, IOException, InputStreamReader, StringReader}
import java.nio.file.Paths

import scala.reflect.io.File


object TestLucene {

  case class Product(id: Int = 0, name: String = null, category: String = null, price: Float = 0, place: String = null, code: String = null)

  @throws[Exception]
  def main(args: Array[String]): Unit = {
    // 1. 准备中文分词器
    val analyzer = new IKAnalyzer
    // 2. 索引
    val index = createIndex(analyzer)
    // 3. 查询器
    val s = new BufferedReader(new InputStreamReader(System.in))
    while (true) {
      print("请输入查询关键字：")
      val keyword = s.readLine()
      println("当前关键字是：" + keyword)
      val query = new QueryParser("name", analyzer).parse(keyword)
      // 4. 搜索
      val reader = DirectoryReader.open(index)
      val searcher = new IndexSearcher(reader)
      val numberPerPage = 5
      val hits = searcher.search(query, numberPerPage).scoreDocs
      // 5. 显示查询结果
      showSearchResults(searcher, hits, query, analyzer)
      // 6. 关闭查询
      reader.close()
    }

  }

  @throws[Exception]
  private def showSearchResults(searcher: IndexSearcher, hits: Array[ScoreDoc], query: Query, analyzer: IKAnalyzer) = {
    val simpleHTMLFormatter = new SimpleHTMLFormatter("<span style='color:red'>", "</span>")
    val highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(query))
    println("找到 " + hits.length + " 个命中.")
    println("序号\t匹配度得分\t结果")
    var i = 0
    while (i < hits.length) {
      val scoreDoc = hits(i)
      val docId = scoreDoc.doc
      val d = searcher.doc(docId)
      val fields = d.getFields
      print(i + 1)
      print("\t" + scoreDoc.score)
      for (f <- fields) {
        if ("name" == f.name) {
          val tokenStream = analyzer.tokenStream(f.name, new StringReader(d.get(f.name)))
          val fieldContent = highlighter.getBestFragment(tokenStream, d.get(f.name))
          print("\t" + fieldContent)
        }
        else print("\t" + d.get(f.name))
      }
      println("<br>")
      i += 1
    }
  }

  @throws[IOException]
  private def createIndex(analyzer: IKAnalyzer):Directory = {
//    val index = new RAMDirectory
    val index=FSDirectory.open(Paths.get("F:\\lucene_model\\"))
    val config = new IndexWriterConfig(analyzer)
    val writer = new IndexWriter(index, config)
    val fileName = "140k_products.txt"
    val products = ProductUtil.file2list(fileName)
    val total = products.length
    var count = 0
    var per = 0
    var oldPer = 0
    for (p <- products) {
      addDoc(writer, p)
      count += 1
      per = count * 100 / total
      if (per != oldPer) {
        oldPer = per
        printf("索引中，总共要添加 %d 条记录，当前添加进度是： %d%% %n", total, per)
      }
    }
    writer.close()
    index
  }

  @throws[IOException]
  private def addDoc(w: IndexWriter, p: Product) = {
    import org.apache.lucene.document.Field
    val doc = new Document()
    doc.add(new TextField("id", p.id.toString, Field.Store.YES))
    doc.add(new TextField("name", p.name, Field.Store.YES))
    doc.add(new TextField("category", p.category, Field.Store.YES))
    doc.add(new TextField("price", p.price.toString, Field.Store.YES))
    doc.add(new TextField("place", p.place, Field.Store.YES))
    doc.add(new TextField("code", p.code, Field.Store.YES))
    w.addDocument(doc)
  }
}
