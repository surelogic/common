/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
//package org.eclipse.cosmos.rm.internal.smlif.editor;
package com.surelogic.common.ui.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Provides a line style for the XML representation of a selected document.
 * 
 * @author Ali Mehregani
 */
public class XMLLineStyler implements LineStyleListener {
  /* The XML Scanner */
  private XMLScanner xmlScanner;

  /* The coloring scheme used */
  private Color[] colorContainer;

  /* A color container */
  private int[] colorIndexContainer;

  /* Indicate the type of the token returned by the scanner */
  protected static final int EOL = -1;
  protected static final int ELEMENT = 0;
  protected static final int ATTRIBUTE = 1;
  protected static final int COMMENT = 2;
  protected static final int OTHER = 3;

  protected static final int NUM_OF_TOKENS = 4;

  public XMLLineStyler() {
    xmlScanner = new XMLScanner();
    Display display = Display.getDefault();
    colorContainer = new Color[] { new Color(display, new RGB(0, 0, 0)), new Color(display, new RGB(0, 0, 128)),
        new Color(display, new RGB(0, 128, 0)), new Color(display, new RGB(128, 0, 0)) };

    colorIndexContainer = new int[NUM_OF_TOKENS];
    colorIndexContainer[ELEMENT] = 1;
    colorIndexContainer[ATTRIBUTE] = 2;
    colorIndexContainer[COMMENT] = 3;
    colorIndexContainer[OTHER] = 0;

  }

  @Override
  public void lineGetStyle(LineStyleEvent event) {
    List<StyleRange> styles = new ArrayList<>();
    int token;
    StyleRange lastStyle;

    xmlScanner.setEventInput(event);
    token = xmlScanner.nextToken();

    while (token != EOL) {
      Color tokenColor = colorContainer[colorIndexContainer[token]];
      StyleRange style = new StyleRange(xmlScanner.getStartOffsetOfToken() + event.lineOffset, xmlScanner.getTokenLength(),
          tokenColor, null);

      if (styles.isEmpty()) {
        styles.add(style);
      } else {
        /* Merge similar styles if possible */
        lastStyle = styles.get(styles.size() - 1);
        if (lastStyle.similarTo(style) && (lastStyle.start + lastStyle.length == style.start))
          lastStyle.length += style.length;
        else
          styles.add(style);
      }

      int startOffset = 0;
      int endOffset = 0;
      do {
        token = xmlScanner.nextToken();
        startOffset = xmlScanner.getStartOffsetOfToken();
        endOffset = startOffset + xmlScanner.getTokenLength();
      } while (token != EOL && startOffset >= 0 && endOffset < event.lineText.length()
          && event.lineText.substring(startOffset, endOffset).trim().length() <= 0);

    }
    event.styles = new StyleRange[styles.size()];
    styles.toArray(event.styles);
  }

  /**
   * The scanner used to parse an XML line
   * 
   * @author Ali Mehregani
   */
  public class XMLScanner {
    private int lineStartOffset;
    private int lineLength;
    private String lineText;

    private int tokenStartOffset;
    private int tokenLength;
    private int lastToken;

    private List<CommentBlock> commentBlocks;

    public XMLScanner() {
      commentBlocks = new ArrayList<>();
    }

    public void setEventInput(LineStyleEvent event) {
      lineStartOffset = event.lineOffset;
      lineLength = event.lineText.length();
      lineText = event.lineText;

      tokenStartOffset = 0;
      tokenLength = 0;

      // updateCommentBlocks(lineStartOffset, lineStartOffset + lineLength);
    }

    public int getTokenLength() {
      return tokenLength;
    }

    public int getStartOffsetOfToken() {
      return tokenStartOffset;
    }

    public int nextToken() {
      tokenStartOffset += tokenLength;
      tokenLength = 0;
      int currentChar = readChar();

      while (currentChar != EOL) {
        if (fallsUnderCommentBlock(lineStartOffset + tokenStartOffset)) {
          while (currentChar != EOL && currentChar != '>') {
            currentChar = readChar();
          }
          if (tokenLength - 3 >= 0
              && lineText.substring(tokenStartOffset + tokenLength - 3, tokenStartOffset + tokenLength).equals("-->"))
            insertEndCommentBlock(lineStartOffset + tokenStartOffset + tokenLength - 3);

          return returnToken(COMMENT);
        }

        switch (currentChar) {
        /* Start of an element or a comment */
        case '<':
          currentChar = readChar();

          /* Element */
          if (currentChar != '!') {
            currentChar = readChar();
            while (currentChar != EOL && currentChar != '>' && currentChar != '=') {
              currentChar = readChar();
            }

            return returnToken(ELEMENT);
          }
          /* Comment */
          else if (lineText.substring(tokenStartOffset).trim().length() >= 4
              && lineText.substring(tokenStartOffset).trim().substring(0, 4).equals("<!--")) {
            int startOfCommentBlock = lineStartOffset + tokenStartOffset + tokenLength - 2;
            skipChar(2);
            currentChar = readChar();
            while (currentChar != EOL && currentChar != '>') {
              currentChar = readChar();
            }

            if (!(tokenStartOffset + tokenLength - 2 > tokenStartOffset + 3 && lineText.substring(
                tokenStartOffset + tokenLength - 3, tokenStartOffset + tokenLength).equals("-->")))
              insertStartCommentBlock(startOfCommentBlock);

            return returnToken(COMMENT);
          }
          break;
        case '\"':
          if (lastToken == ELEMENT) {
            currentChar = readChar();
            while (currentChar != EOL && currentChar != '\"') {
              currentChar = readChar();
            }

            return returnToken(ATTRIBUTE);
          } else {
            skipUntilEnd();
            return returnToken(ELEMENT);
          }
        default:
          if (lastToken == ATTRIBUTE) {
            while (currentChar != EOL && currentChar != '=' && currentChar != '>') {
              currentChar = readChar();
            }

            return returnToken(ELEMENT);
          } else if (currentChar == '/' && (lastToken == ATTRIBUTE || lastToken == ELEMENT)) {
            if (readChar() == '>')
              return returnToken(ELEMENT);
            else
              return returnToken(OTHER);
          } else {
            while (currentChar != EOL && currentChar != '<') {
              currentChar = readChar();
            }
            if (currentChar == '<')
              unreadChar();

            return returnToken(OTHER);
          }

        }
      }
      return EOL;
    }

    private void unreadChar() {
      tokenLength--;
    }

    private int returnToken(int token) {
      lastToken = token;
      return token;
    }

    private void skipUntilEnd() {
      tokenLength = lineLength;
    }

    private int readChar() {
      if (tokenStartOffset + tokenLength >= lineLength)
        return EOL;

      int character = lineText.charAt(tokenStartOffset + tokenLength++);

      /* Skip white spaces */
      if (isWhiteSpace(character))
        return readChar();

      return character;
    }

    private void skipChar(int numOfChar) {
      for (int i = 0; i < numOfChar; i++)
        readChar();
    }

    private boolean isWhiteSpace(int characterInt) {
      char character = (char) characterInt;
      if (character == ' ' || character == '\t' || character == '\f')
        return true;
      return false;
    }

    private void insertStartCommentBlock(int start) {
      for (int i = 0, commentBlockCount = commentBlocks.size(); i < commentBlockCount; i++) {
        CommentBlock commentBlock = commentBlocks.get(i);
        if (commentBlock.start == start)
          return;
      }

      CommentBlock commentBlock = new CommentBlock();
      commentBlock.start = start;
      commentBlocks.add(commentBlock);
    }

    private void insertEndCommentBlock(int end) {
      CommentBlock commentBlock;
      CommentBlock lastEligibleCommentBlock = null;

      for (int i = 0, commentBlockCount = commentBlocks.size(); i < commentBlockCount; i++) {
        commentBlock = commentBlocks.get(i);
        if (end > commentBlock.start && commentBlock.end == -1
            && (lastEligibleCommentBlock == null || lastEligibleCommentBlock.start > commentBlock.start))
          lastEligibleCommentBlock = commentBlock;
      }

      if (lastEligibleCommentBlock != null)
        lastEligibleCommentBlock.end = end;
    }

    /* NOT needed */
    // private void updateCommentBlocks(int startLineOffset, int endLineOffset)
    // {
    // for (int i = 0; i < commentBlocks.size(); i++)
    // {
    // CommentBlock commentBlock = (CommentBlock) commentBlocks.get(i);
    // if ((startLineOffset <= commentBlock.start && commentBlock.start <=
    // endLineOffset))
    // commentBlocks.remove(commentBlock);
    // else if (startLineOffset <= commentBlock.end && commentBlock.end >=
    // endLineOffset)
    // commentBlocks.remove(commentBlock);
    // }
    // }

    private boolean fallsUnderCommentBlock(int offset) {
      CommentBlock commentBlock;
      for (int i = 0, commentBlockCount = commentBlocks.size(); i < commentBlockCount; i++) {
        commentBlock = commentBlocks.get(i);
        if (offset > commentBlock.start && (commentBlock.end == -1 || offset < commentBlock.end))
          return true;
      }

      return false;
    }

    private class CommentBlock {
      protected int start;
      protected int end;

      public CommentBlock() {
        start = -1;
        end = -1;
      }
    }
  }

  protected void dispose() {
    for (int i = 0; i < colorContainer.length; i++)
      colorContainer[i].dispose();
  }
}
