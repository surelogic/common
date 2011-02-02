package com.surelogic.common.eclipse;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.surelogic.common.CommonImages;

public final class RadioArrowMenu {
  private List<Object> f_choices = new ArrayList<Object>();
  private final Map<Object, Composite> f_choiceToComposite = new HashMap<Object, Composite>();
  private Object f_selectedChoice = null;
  private Object f_focusChoice = null;
  private boolean f_enabled = true;
	private final Composite f_panel;
	
	private final FocusListener f_focusListener = new FocusListener() {
    public void focusGained(FocusEvent e) {
      paintFocus(true);
    }
    public void focusLost(FocusEvent e) {
      paintFocus(false);
    } 
    private void paintFocus(boolean hasFocus) {
      if (f_focusChoice != null) {
        Composite button = f_choiceToComposite.get(f_focusChoice);
        focus(button, hasFocus); // Repaints the appropriate color
      }
    }
	};
	
	private final Listener f_traverseListener = new Listener() {
    public void handleEvent(Event e) {
      switch (e.detail) {
        case SWT.TRAVERSE_ESCAPE:
          setCustomTabTraversal(e);
          notifyObserversEscape();
          break;
        case SWT.TRAVERSE_RETURN:
          setCustomTabTraversal(e);
          // Take the focus and select it
          Object choice = f_focusChoice;
          if (choice == null) {
            if (f_choices.isEmpty()) {
              return;
            }
            choice = f_choices.get(0);
          } 
          Composite tlParent = f_choiceToComposite.get(choice);
          selected(tlParent, choice, true);          
          notifyObserversGoNext();
          break;
        case SWT.TRAVERSE_TAB_NEXT:
          setCustomTabTraversal(e);
          notifyObserversGoNext();
          break;
        case SWT.TRAVERSE_TAB_PREVIOUS:
          setCustomTabTraversal(e);
          notifyObserversGoBack();
          break;        
      }
    }
	};
	
	private final KeyListener f_keyListener = new KeyListener() {
	  public void keyPressed(KeyEvent e) {
	    switch (e.keyCode) {
	      default:
	        if (f_choices.isEmpty()) {
	          return;
	        }
	        if (e.character == ' ') {
	          break;
	        }
	        return;
	      case SWT.ARROW_LEFT:
	        notifyObserversGoBack();
	        break;
	      case SWT.ARROW_RIGHT:
	        notifyObserversGoNext();
	        break;
	      case SWT.ARROW_DOWN:	        
	      case SWT.ARROW_UP:
	        if (f_choices.isEmpty()) {
	          return;
	        }
	        break;
	    }
	    // At least one choice
	    int i = (f_focusChoice == null) ? 0 : f_choices.indexOf(f_focusChoice);
	    if (e.keyCode == SWT.ARROW_UP) {
	      i--;
	      if (i < 0 || i >= f_choices.size()) {
	        // last choice (e.g. wrap)
	        i = f_choices.size() - 1; 
	      }
	    } 
	    else if (e.keyCode == SWT.ARROW_DOWN) {
	      i++;
	      if (i < 0 || i >= f_choices.size()) {
	        // first choice (e.g. wrap)
	        i = 0; 
	      }
	    }	    
	    Object choice = f_choices.get(i);
	    if (choice != null) {
	      Composite tlParent = f_choiceToComposite.get(choice);
	      if (e.character == ' ') {
          selected(tlParent, choice, true); 
	      } else {
	        focused(tlParent, choice);  
	      }
	    }	    
	  }

	  public void keyReleased(KeyEvent e) {
	    // Nothing to do
	  }
	};
	
  protected static void setCustomTabTraversal(Event e) {
    // i.e. we'll take care of things
    e.doit = true;
    e.detail = SWT.TRAVERSE_NONE;
  }
	
	public Composite getPanel() {
		return f_panel;
	}

	public RadioArrowMenu(Composite panel) {
		assert panel != null;
		f_panel = panel;
		final RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.fill = true;
		panel.setLayout(layout);
		
		panel.addKeyListener(f_keyListener);
		panel.addListener(SWT.Traverse, f_traverseListener);
		panel.addFocusListener(f_focusListener);
	}

	public void addChoice(Object choice, Image image) {
		if (choice == null)
			throw new IllegalArgumentException("choice must be non-null");
		Composite button = constructChoice(choice, image, f_panel);

		if (f_focusChoice == null) {
		  focused(button, choice); // Set to be the first one
		}
	}

	public void addSeparator() {
		constructSeparator(f_panel);
	}

	public void forceFocus() {	 
	  f_panel.forceFocus();
	}

	public boolean isFocusControl() {
	  return f_panel.isFocusControl();
	}
	
	public boolean isEnabled() {
		return f_enabled;
	}

	public void setEnabled(boolean enabled) {
		f_enabled = enabled;
		setEnabledHelper(f_panel, enabled);
	}

	private void setEnabledHelper(Composite c, boolean enabled) {
		if (c.isDisposed())
			return;
		final Display display = c.getShell().getDisplay();
		final Composite selected = f_choiceToComposite.get(f_selectedChoice);
		for (Control control : c.getChildren()) {
			if (control instanceof Composite) {
				setEnabledHelper((Composite) control, enabled);
			} else if (control instanceof Label) {
				final Label l = (Label) control;
				l.setEnabled(enabled);
				if (!l.getText().equals("")) {
					// Nothing to do
				}
				if (enabled) {
					if (l.getParent().getParent() == selected) {
						l.setForeground(display
								.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
					} else {
						l.setForeground(null);
					}
				} else {
					l
							.setForeground(display
									.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
				}
			}
		}
	}

	public interface IRadioMenuObserver {
		void selected(Object choice, RadioArrowMenu menu);
		void escape(RadioArrowMenu menu);
		void goBack(RadioArrowMenu menu);		
		void goNext(RadioArrowMenu menu);
	}

	private final Set<IRadioMenuObserver> f_selectionObservers = new CopyOnWriteArraySet<IRadioMenuObserver>();

	public void addObserver(IRadioMenuObserver o) {
		if (o == null)
			return;
		f_selectionObservers.add(o);
	}

	public void removeObserver(IRadioMenuObserver o) {
		f_selectionObservers.remove(o);
	}

	private void notifyObservers(Object choice) {
		for (IRadioMenuObserver o : f_selectionObservers)
			o.selected(choice, this);
	}
	
	private void notifyObserversGoBack() {
	  for (IRadioMenuObserver o : f_selectionObservers)
	    o.goBack(this);
	}
	
	private void notifyObserversGoNext() {
	  for (IRadioMenuObserver o : f_selectionObservers)
	    o.goNext(this);
	}
	
	private void notifyObserversEscape() {
	  for (IRadioMenuObserver o : f_selectionObservers)
	    o.escape(this);
	}

	private void constructSeparator(Composite parent) {
		new Label(parent, SWT.NONE);
	}

	private Composite constructChoice(final Object choice, Image image,
			Composite parent) {
		final Composite result = new Composite(parent, SWT.NONE);
		FillLayout fill = new FillLayout(SWT.HORIZONTAL);
		fill.marginHeight = 1;
		fill.marginWidth = 1;
		result.setLayout(fill);	
		
    final Composite mezzanine = new Composite(result, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.numColumns = 3;
    mezzanine.setLayout(layout);    
    //mezzanine.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, true, false));  
    
		final Label prefixImage = new Label(mezzanine, SWT.NONE);
		prefixImage.setImage(image);
		prefixImage.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, false,
				false));
		final Label textLabel = new Label(mezzanine, SWT.LEFT);
		textLabel.setText(choice.toString());
		textLabel
				.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		final Label arrowImage = new Label(mezzanine, SWT.RIGHT);
		arrowImage.setImage(SLImages.getImage(CommonImages.IMG_RIGHT_ARROW_SMALL));
		arrowImage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		f_choiceToComposite.put(choice, result);
		textLabel.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				selected(result, choice, true);
			}
		});
    int i = f_choices.indexOf(choice);
    if (i < 0) {
      f_choices.add(choice);
    } else {
      f_choices.set(i, choice);
    }
    return result;
	}

	private void selected(Composite button, Object choice,
			boolean notifyObservers) {
	  focused(button, choice);
	  
		if (f_selectedChoice != null) {
			// already selection?
			if (f_selectedChoice == choice)
				return;
			
			Composite oldButton = f_choiceToComposite.get(f_selectedChoice);
      unhighlight(oldButton);
		}
    f_selectedChoice = choice;
		highlight(button);
    forceFocus();
		
		if (notifyObservers)
			notifyObservers(choice);
	}

  private void checkConsistency(Composite button, Object choice) {
    Composite mapped = f_choiceToComposite.get(choice);
		if (mapped != button) {
		  //System.out.println(button+" doesn't match "+mapped);
		  f_choiceToComposite.put(choice, button);
		}
  }

	public void clearSelection() {
		if (f_selectedChoice != null) {
			unhighlight(f_choiceToComposite.get(f_selectedChoice));
			f_selectedChoice = null;
		}
	}

	public void setSelection(Object o) {
		final Composite parent = f_choiceToComposite.get(o);
		if (parent != null) {
			/*
			 * Do not notify observers that this was selected, consistent with
			 * SWT check buttons.
			 */
			selected(parent, o, false);
		}
	}

	private void highlight(Composite button) {
		final Display display = button.getShell().getDisplay();
		Color selected = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
	  setBackground(button, selected);
		setLabelTextColorDeep(button, display
				.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
	}

	private void unhighlight(Composite button) {
		setBackground(button, null);		
		setLabelTextColorDeep(button, null);
	}

  private void setBackground(Composite button, Color color) {
    // Leave the button unchanged, but change its'children
    for (Control c : button.getChildren()) {
      setBackground_internal(c, color);
    }
  }
  
  private void setBackground_internal(Control c, Color color) {
    c.setBackground(color);
    if (c instanceof Composite) {
      setBackground((Composite) c, color);
    }
  }

	private void setLabelTextColorDeep(Control c, Color color) {
		if (c instanceof Composite) {
			for (Control child : ((Composite) c).getChildren()) {
				setLabelTextColorDeep(child, color);
			}
		} else {
			if (c instanceof Label) {
				Label l = (Label) c;
				if (!l.getText().equals("")) {
					l.setForeground(color);
				}
			}
		}
	}
	/*
	private void setAlignmentDeep(Control c, int alignment) {
	  if (c instanceof Composite) {
	    for (Control child : ((Composite) c).getChildren()) {
	      setAlignmentDeep(child, alignment);
	    }
	  } else {
	    if (c instanceof Label) {
	      Label l = (Label) c;
	      if (!l.getText().equals("")) {
	        l.setAlignment(alignment);        
	      }
	    }
	  }
	}
	*/
	private void focused(Composite button, Object choice) {
	  if (f_focusChoice != null) {
	    // already focused?
	    if (f_focusChoice == choice)
	      return;
	    unfocus(f_choiceToComposite.get(f_focusChoice));
	  }
	  checkConsistency(button, choice);
	  f_focusChoice = choice;
	  focus(button);
	  forceFocus();
	}
	
	private void unfocus(Composite button) {
	  button.setBackground(null);
    //setAlignmentDeep(button, SWT.LEFT);
	}
	
  private void focus(Composite button) {
    focus(button, f_panel.isFocusControl());
  }
  
  private void focus(Composite button, boolean hasFocus) {
    final Display display = button.getShell().getDisplay();
    Color focused;
    if (hasFocus) { 
      focused = display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
    } else { 
      focused = display.getSystemColor(SWT.COLOR_BLACK);
    }
    button.setBackground(focused);
    //setAlignmentDeep(button, SWT.RIGHT);    
  }
}
