package gcom.client.gui

import swing._
import swing.event._
import java.awt.FlowLayout

import gcom.Communicator
import gcom.client._

class ChatGui (comm : Communicator) extends SimpleSwingApplication {

  val communicator = comm

  def top = new MainFrame {
    title = "ChatGui"

    import Dialog._
    val buttons = new BoxPanel(Orientation.Vertical) {

      }

    communicator.setOnReceive({ msg =>
      chatBox.append("\n" + msg)
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        def run() {
          val bar = chatScroller.verticalScrollBar
          bar.value = bar.maximum
        }
        });
    });
    object button extends Button {
      text = "Send"
    }
    object chatBox extends TextArea{
      text = "Welcome to chat"
      editable = false;

    }
    object chatScroller extends ScrollPane{
      contents = chatBox;
    }
    object chatInput extends TextField(30){
      horizontalAlignment = Alignment.Left;
    };

    object nodeList extends ListView(List("Waiting for group info"))

    object inputPanel extends FlowPanel{
      contents.append(chatInput, button);
    }

    contents = new BoxPanel(Orientation.Vertical) {
      contents.append(new BoxPanel(Orientation.Horizontal){
       contents.append(chatScroller, new ScrollPane(nodeList))
      }, inputPanel)
      border = Swing.EmptyBorder(5, 5, 5, 5)
    }

    listenTo(button)
    listenTo(chatInput)
    reactions += {
      case ButtonClicked(button) =>
        communicator.broadcastMessage(chatInput.text)
      case EditDone(_) =>
        communicator.broadcastMessage(chatInput.text)
    }

    override def closeOperation(){
      communicator.leaveGroup
      super.closeOperation
    }
  }

}
