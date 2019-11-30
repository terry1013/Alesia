package gui.console;

import java.awt.event.*;
import java.time.*;
import java.util.logging.*;

import javax.swing.*;

class TextAreaLogger {
}

class EnterAction extends AbstractAction {
  private static final Logger LOGGER = Logger.getLogger(TextAreaLogger.class.getName());
  private final JTextField textField;

  protected EnterAction(JTextField textField) {
    super("Enter");
    this.textField = textField;
  }

  @Override public void actionPerformed(ActionEvent e) {
    LOGGER.info(String.format("%s%n  %s%n", LocalDateTime.now().toString(), textField.getText()));
  }
}
