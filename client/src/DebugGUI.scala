import swing._
import swing.event._
import java.awt.FlowLayout
import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel
import com.sun.java.swing.plaf.gtk.GTKLookAndFeel
import javax.swing.{UIManager}
import java.awt.GridBagConstraints
import java.awt.GridBagConstraints
import gcom.Group
import gcom.common._
import gcom.transport.Transport
import gcom.Communicator
import gcom.ordering.Ordering

/**
 * Provides 
 * top                    : Returns a mainframe with debugging GUI
 * sendFunction           : The function to execute when a message is received
 * showGroupSelectDialog  : Shows a dialog where the user can select a group
 */
object DebugGui {

  //initialize NameServer
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

  var messageQueues = Map[String,List[String]]();
  var messageCounter = 0
  
  def showGroupSelectDialog(possibilities: List[Group]): Option[Group] = {
    import Dialog._
    val buttons = new BoxPanel(Orientation.Vertical)
    val groupToJoin = showInput[Group](buttons,
      "Select a server",
      "Server Selection",
      scala.swing.Dialog.Message.Plain,
      Swing.EmptyIcon,
      possibilities, null)
    groupToJoin
  }
  
  
  object chatBox extends TextArea{
    text = "Welcome to chat"
    editable = false;
  }

  object scrollingChatBox extends ScrollPane{
    contents = chatBox;
  }

  def sendFunction(msg: String) {
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
  }
  def top(t: Transport, o: Ordering, c: Communicator, group: Group) = new MainFrame {
    title = "ChatGui " + group.toString

    val com = c

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


    object nodeList extends ListView[String]{
      listData = List("Waiting for node info...")
    }

    object counter extends Label("0 Messages")


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
        val key = queueList.listData(queueList.selection.anchorIndex)
        messageList.listData = messageQueues(key)
      }
      case UpdateSentMessages(num) => {
        messageCounter += 1
        counter.text = messageCounter.toString + " Messages"
      }
    }
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
       messageCounter = 0
       com.broadcastMessage(chatInput.text)
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
