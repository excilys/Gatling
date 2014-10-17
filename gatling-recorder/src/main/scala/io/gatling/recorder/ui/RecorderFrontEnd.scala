/**
 * Copyright 2011-2014 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.recorder.ui

import scala.swing.Dialog
import scala.swing.Swing.onEDT

import io.gatling.core.util.PathHelper._
import io.gatling.recorder.{ Proxy, RecorderMode }
import io.gatling.recorder.controller.RecorderController
import io.gatling.recorder.ui.swing.component.DialogFileSelector
import io.gatling.recorder.ui.swing.frame.{ ConfigurationFrame, RunningFrame }

object RecorderFrontend {

  // Currently hardwired to the Swing frontend
  // Will select the desired frontend when more are implemented
  def newSwingFrontend(controller: RecorderController): RecorderFrontend =
    new SwingFrontend(controller)

  def newHeadlessFrontend(controller: RecorderController): RecorderFrontend =
    new HeadlessFrontend(controller)
}

sealed abstract class RecorderFrontend(controller: RecorderController) {

  /******************************/
  /**  Controller => Frontend  **/
  /******************************/

  def selectedMode: RecorderMode

  def harFilePath: String

  def handleMissingHarFile(path: String): Unit

  def handleHarExportSuccess(): Unit

  def handleHarExportFailure(message: String): Unit

  def handleFilterValidationFailures(failures: Seq[String]): Unit

  def askSimulationOverwrite: Boolean

  def init(): Unit

  def recordingStarted(): Unit

  def recordingStopped(): Unit

  def receiveEventInfo(eventInfo: EventInfo): Unit

  /******************************/
  /**  Frontend => Controller  **/
  /******************************/

  def addTag(tag: String): Unit = controller.addTag(tag)

  def startRecording(): Unit = controller.startRecording()

  def stopRecording(save: Boolean): Unit = controller.stopRecording(save)

  def clearRecorderState(): Unit = controller.clearRecorderState()
}

private class HeadlessFrontend(controller: RecorderController) extends RecorderFrontend(controller) {
  override def selectedMode: RecorderMode = Proxy

  override def receiveEventInfo(eventInfo: EventInfo): Unit =
    log(s"[RECORD EVENT] ${eventInfo}")

  override def init(): Unit = {}

  override def handleHarExportFailure(message: String): Unit = ???

  override def harFilePath: String = null

  override def handleHarExportSuccess(): Unit = ???

  override def recordingStarted(): Unit =
    log("Recording started.")

  override def handleFilterValidationFailures(failures: Seq[String]): Unit = ???

  override def recordingStopped(): Unit =
    log("Recording stopped.")

  override def askSimulationOverwrite: Boolean = {
    log(
      """Could not start as simulation file already exists.
        |  Try setting class, package or path to change the target file.
        |  Call the recorder with `--help` switch for more info.
        |
        |TODO: it would be nice if there was a force-override option!""".stripMargin)
    return false
  }

  override def handleMissingHarFile(path: String): Unit = ???

  private def log(message: String) =
    println(s"[HEADLESS] ${message}")
}

private class SwingFrontend(controller: RecorderController) extends RecorderFrontend(controller) {

  private lazy val runningFrame = new RunningFrame(this)
  private lazy val configurationFrame = new ConfigurationFrame(this)

  def selectedMode = configurationFrame.selectedMode

  def harFilePath = configurationFrame.harFilePath

  def handleMissingHarFile(harFilePath: String): Unit = {
    if (harFilePath.isEmpty) {
      Dialog.showMessage(
        title = "Error",
        message = "You haven't selected an HAR file.",
        messageType = Dialog.Message.Error)
    } else {
      val possibleMatches = lookupFiles(harFilePath)
      if (possibleMatches.isEmpty) {
        Dialog.showMessage(
          title = "No matches found",
          message = """	|No files that could closely match the
									|selected file's name have been found.
									|Please check the file's path is correct.""".stripMargin,
          messageType = Dialog.Message.Warning)
      } else {
        val selector = new DialogFileSelector(configurationFrame, possibleMatches)
        selector.open()
        val parentPath = harFilePath.getParent
        configurationFrame.updateHarFilePath(selector.selectedFile.map(file => (parentPath / file).toString))
      }
    }
  }

  def handleHarExportSuccess(): Unit = {
    Dialog.showMessage(
      title = "Conversion complete",
      message = "Successfully converted HAR file to a Gatling simulation",
      messageType = Dialog.Message.Info)
  }

  def handleHarExportFailure(message: String): Unit = {
    Dialog.showMessage(
      title = "Error",
      message = s"""	|Export to HAR File unsuccessful: $message.
							|See logs for more information""".stripMargin,
      messageType = Dialog.Message.Error)
  }

  def handleFilterValidationFailures(failures: Seq[String]): Unit = {
    Dialog.showMessage(
      title = "Error",
      message = failures.mkString("\n"),
      messageType = Dialog.Message.Error)
  }

  def askSimulationOverwrite = {
    Dialog.showConfirmation(
      title = "Warning",
      message = "You are about to overwrite an existing simulation.",
      optionType = Dialog.Options.OkCancel,
      messageType = Dialog.Message.Warning) == Dialog.Result.Ok
  }

  def init(): Unit = {
    configurationFrame.visible = true
    runningFrame.visible = false
  }

  def recordingStarted(): Unit = {
    runningFrame.visible = true
    configurationFrame.visible = false
  }

  def recordingStopped(): Unit = runningFrame.clearState()

  def receiveEventInfo(eventInfo: EventInfo): Unit = onEDT(runningFrame.receiveEventInfo(eventInfo))

  private def lookupFiles(path: String): List[String] = {
    val parent = path.getParent
    parent.files.filter(_.path.startsWith(path)).map(_.filename).toList
  }
}
