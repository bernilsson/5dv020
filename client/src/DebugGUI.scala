import swing._
import swing.event._
import java.awt.FlowLayout
import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel
import com.sun.java.swing.plaf.gtk.GTKLookAndFeel
import javax.swing.{UIManager}
import java.awt.GridBagConstraints
import java.awt.GridBagConstraints
/*
class DummyCommunicator(callBack: String => Unit){
  def broadCastMsg(msg: String){
      callBack(msg);
  }
  def leaveGroup() = {println( "Leaving" )}
}

object DummyNameServer{
  def joinGroup(g: JoinGroup, onRecv: String => Unit) = {
    new Thread(new Runnable{
      def run(){
        for(i <- 1 to 100){
          onRecv("Hello " + i)
          Thread.sleep(100);
        }
      }
    }).start();

    new DummyCommunicator(onRecv);
  }
  def listGroups(): List[Group] = {
    List(Group("Group1", Reliable(),FIFO()),
    * Group("Group2", NonReliable(),Total()));
  }
}
*/
object DebugGui extends SimpleSwingApplication {

  //initialize NameServer
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

  var messageQueues = Map[String,List[Message]]();

  def top = new MainFrame {
    title = "ChatGui"

    /* Display a dialog for user to select group before showing gui */
    import Dialog._
    val possibilities = DummyNameServer.listGroups;
    val buttons = new BoxPanel(Orientation.Vertical)
    val s = showInput[Group](buttons,
      "Select a server",
      "Server Selection",
      Message.Plain,
      Swing.EmptyIcon,
      possibilities, null)

    if(s.isEmpty){
      quit()
    }

    val com = DummyNameServer.joinGroup(
        ExistingGroup(s.get.groupName),
        { msg =>

          chatBox.append("\n" + msg)
          /* Work around bug where maximum was not yet updated, let swing
           * handle scrolling down.
           */
          javax.swing.SwingUtilities.invokeLater(new Runnable() {
                  def run() {
                      val bar = scrollingChatBox.verticalScrollBar
                      bar.value = bar.maximum
                  }
              }
          );
         });

    object dropText extends Label("Delay: ")
    object dropInput extends CheckBox
    object delayText extends Label("Drop: ")
    object delayInput extends TextField{
      columns = 3;
    }

    object queueList extends ListView[String]{
      listData = List("No queues...")
    }

    object messageList extends ListView[String]{
      listData = List("Messages are to be displayed here")
    }


    object button extends Button {
      text = "Send"
    }
    object chatBox extends TextArea{
      text = "Welcome to chat"
      editable = false;
      listenTo(button)
      listenTo(chatInput)
      reactions += {
        case ButtonClicked(button) => sendMsg()
        case EditDone(_) => sendMsg()
      }
      def sendMsg() = {
         val drop = dropInput.selected;
         val delay = try{
            delayInput.text.toInt
            } catch{
              case e: NumberFormatException =>
                0
            }
          com.broadCastMsg(chatInput.text)
      }

    }

    object scrollingChatBox extends ScrollPane{
      contents = chatBox;
    }
    object nodeList extends ListView[String]{
      listData = List("Waiting for node info...")
    }

    object counter extends Label("0 msg/sec")


    object chatInput extends TextField{
      preferredSize = new Dimension(250,30)
      horizontalAlignment = Alignment.Left;
    };

    object inputPanel extends FlowPanel{
      //For some reason objects are not displayed in the same order as added
      contents.append(counter,
          chatInput,
          delayText,
          dropInput,
          dropText,
          delayInput,
          button);
    }

    contents = new GridBagPanel {
      import java.awt.GridBagConstraints._;
      val chatAndGroupMembers = new GridBagPanel {
        add(scrollingChatBox, new Constraints(){
          grid = (0,0)
          gridheight = 1
          gridwidth = 1
          weightx = 1
          weighty = 1
          anchor = GridBagPanel.Anchor.LineStart
          fill = GridBagPanel.Fill.Both
        })
        add(new ScrollPane(nodeList), new Constraints(){
          grid = (1,0)
          gridheight = 1
          gridwidth = 1
          weightx = 0.2
          weighty = 1
          anchor = GridBagPanel.Anchor.LineStart
          fill = GridBagPanel.Fill.Both

        })
      }
      //contents.append(scroller, inputPanel)
      addComponent(new ScrollPane(queueList),  0, 0, 1, 1, 1 ,1, NORTH,  BOTH)
      addComponent(new ScrollPane(messageList),1, 0, 1, 1, 1 ,1, NORTH,  BOTH)
      addComponent(chatAndGroupMembers,        0, 1, 2, 1, 1 , 1, NORTH, BOTH)
      addComponent(inputPanel,                 0, 2, 2, 1, 1 , 0, NORTH, BOTH)

      border = Swing.EmptyBorder(5, 5, 5, 5)

      def addComponent(component: Component, x: Int, y: Int,
                       width: Int, height: Int,
                       weightx: Double, weighty: Double,
                       anchor: Int, fill: Int): Unit = {
      val gbc = new GridBagConstraints
      gbc.gridx = x
      gbc.gridy = y
      gbc.gridwidth = width
      gbc.gridheight = height
      gbc.weightx = weightx
      gbc.weighty = weighty
      gbc.anchor = anchor
      gbc.fill = fill
      add(component,new Constraints(gbc))
    }

    }

    case class UpdateQueue(name: String,list: List[Message]) extends Event
    case class UpdateSentMessages(num: Double) extends Event

    listenTo(queueList.selection)
    reactions += {
      case UpdateQueue(name, list) => {
        val selection = queueList.selection.anchorIndex;
        messageQueues += (name -> list)
        updateList();
        queueList.selectIndices(selection)

      }
      // `` lets us match against that specific variable.
      case ListSelectionChanged(`queueList`,range,live) => {
        val data = queueList.listData(queueList.selection.anchorIndex)
        messageList.listData = data.map(_.toString)
      }
      case UpdateSentMessages(num) => {
        counter.text = num.toString + " m/sec "
      }
    }

    private def updateList() = {
      queueList.listData_=(messageQueues.keySet.toSeq)
    }

    override def closeOperation(){
      com.leaveGroup
      super.closeOperation
    }
  }

}
