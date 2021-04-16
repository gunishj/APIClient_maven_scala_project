import java.io.FileInputStream
import java.util.Properties
import org.apache.log4j.Logger


class PropReader {

  def propReader (path : String) : Properties = {
    val prop = new Properties()
    val log = Logger.getLogger(getClass.getName);
        try{
          val input = new FileInputStream(path)
          prop.load(input)
        }
        catch {
          case ex: Exception => log.error ("Exception in loading the properties file " + ex.getMessage)
          ex.printStackTrace()
          System.exit(-1)

        }
    prop
  }

}
