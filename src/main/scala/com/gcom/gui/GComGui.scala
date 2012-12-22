package com.gcom.gui

import scala.swing._

object HelloWorld extends SimpleSwingApplication {
	def top = new MainFrame {
		title = "Hello, World!"
				contents = new Button {
		  action = Action("Clock Me"){
		    println("Clicked")
		  }
			text = "Click Me!"
		}
	}
}


