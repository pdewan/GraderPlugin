package com.unc.cs.graderprogramplugin.views;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.part.ViewPart;

import com.unc.cs.graderprogramplugin.com.GraderCommunicator;
import com.unc.cs.graderprogramplugin.com.OnyenAuthenticator;
import com.unc.cs.graderprogramplugin.com.sql.DatabaseReader;
import com.unc.cs.graderprogramplugin.com.sql.IDatabaseReader;
import com.unc.cs.graderprogramplugin.com.sql.SQLConstants;
import com.unc.cs.graderprogramplugin.utils.AssignmentFinder;
import com.unc.cs.graderprogramplugin.utils.FileWriter;
import com.unc.cs.graderprogramplugin.utils.ZipWriter;

/**
 * 
 * @author Andrew Vitkus
 */

public class SendAssignmentView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.unc.cs.graderprogramplugin.views.SendAssignmentView";

	private Combo assignmentTypeCombo;
	private Combo assignmentsCombo;
	private Combo courseCombo;
	private Button sendButton;
	private Label onyenLabel;
	private Text onyenText;
	private Label passwordLabel;
	private Text passwordText;
	private Shell myShell;
	
	private IProject[] assignmentProjects;
	
	/**
	 * The constructor.
	 */
	public SendAssignmentView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		myShell = parent.getShell();
		
		parent.setLayout(new GridLayout(2, false));
		
		courseCombo = new Combo(parent, SWT.READ_ONLY);
		courseCombo.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false, 2, 1));
		courseCombo.setText("Course");
		courseCombo.setToolTipText("The course the assignment is for.");
		IDatabaseReader dr = new DatabaseReader();
		try {
			dr.connect(SQLConstants.username, SQLConstants.password, SQLConstants.server);
			courseCombo.setItems(dr.readCourseList());
		} catch (SQLException e1) {
			//e1.printStackTrace();
		} finally {
			try {
				dr.disconnect();
			} catch (SQLException e) {
				//e.printStackTrace();
			}
		}
		if (courseCombo.getItemCount() > 0) {
			courseCombo.select(0);
		}

		assignmentTypeCombo = new Combo(parent, SWT.READ_ONLY);
		assignmentTypeCombo.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false, 2, 1));
		assignmentTypeCombo.setText("Assignment Type");
		assignmentTypeCombo.setToolTipText("Choose a type");
		try {
			dr.connect(SQLConstants.username, SQLConstants.password, SQLConstants.server);
			assignmentTypeCombo.setItems(dr.readAssignmentTypes());
			assignmentTypeCombo.select(0);
		} catch (SQLException e1) {
			//e1.printStackTrace();
		} finally {
			try {
				dr.disconnect();
			} catch (SQLException e) {
				//e.printStackTrace();
			}
		}
		if (courseCombo.getItemCount() > 0) {
			courseCombo.select(0);
		}
		
		assignmentsCombo = new Combo(parent, SWT.READ_ONLY);
		assignmentsCombo.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false, 2, 1));
		assignmentsCombo.setText("Assignments");
		assignmentsCombo.setToolTipText("Choose an open assignment");

		ISecurePreferences root = SecurePreferencesFactory.getDefault();
		final ISecurePreferences node = root.node("/com/unc");
		
		String course = readPreference(node, "course", null);
		if (course != null) {
			courseCombo.select(courseCombo.indexOf(course));
		}
		
		onyenLabel = new Label(parent, SWT.LEFT);
		onyenLabel.setText("Onyen");
		
		
		onyenText = new Text(parent, SWT.LEFT | SWT.BORDER);
		onyenText.setToolTipText("Your UNC Onyen");
		onyenText.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		String onyen = readPreference(node, "onyen", null);
		if (onyen != null) {
			onyenText.setText(onyen);
		}
		
		passwordLabel = new Label(parent, SWT.LEFT);
		passwordLabel.setText("Password");
		
		passwordText = new Text(parent, SWT.PASSWORD | SWT.BORDER);
		passwordText.setToolTipText("Your UNC Onyen password");
		passwordText.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		String password = readPreference(node, "password", null);
		if (password != null) {
			passwordText.setText(password);
		}

		sendButton = new Button(parent, SWT.PUSH );
		sendButton.setLayoutData(new GridData (SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		sendButton.setText("Send Assignment");
		sendButton.setToolTipText("Send assignment to the grading server");

		buildCombo(assignmentTypeCombo.getText());
		attachListeners();
		
		if (assignmentsCombo.getItemCount() > 0) {
			assignmentsCombo.select(0);
		}
		
	}
	
	private void attachListeners() {
		sendButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IProject p = assignmentProjects[assignmentsCombo.getSelectionIndex()];
				final File projectDir = new File(p.getLocationURI());
				File initTarget = null;
				try {
					initTarget = File.createTempFile(projectDir.getParentFile().getPath() + System.getProperty("file.separator") + p.getName(), ".zip");
					ZipWriter.zip(projectDir, initTarget);
				} catch (IOException ex) {
					showMessage("Failed to prep assignment for submission!");
					if (initTarget.exists()) {
						initTarget.delete();
					}
					ex.printStackTrace();
					return;
				}
				final File target = initTarget;
				final String assignmentSelection = assignmentsCombo.getText();
				final String courseSelection = courseCombo.getText();
				final String onyen = onyenText.getText();
				final String password = passwordText.getText();
				new Thread() {
					@Override
					public void run() {
						GraderCommunicator com = null;
						try {
							com = new GraderCommunicator();
							String vfykey = OnyenAuthenticator.authenticate(onyen, password);
							com.sendAssignment(target, assignmentSelection, courseSelection, vfykey);
							try {
								com.getBooleanResponse();
							} catch (SocketException ex) {
								myShell.getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										showMessage("Authentication failed!");
									}
								});
								return;
							}
							String response = com.getUTFResponse();
							final File responseFile = File.createTempFile(projectDir.getPath() + System.getProperty("file.separator") + "grade", ".html");
							FileWriter.write(response, responseFile);
							
							myShell.getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									try {
										IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(null);
										browser.openURL(responseFile.toURI().toURL());
									} catch (PartInitException e1) {
										showMessage("Submission failed!");
										//e1.printStackTrace();
									} catch (MalformedURLException e1) {
										showMessage("Submission failed!");
										//e1.printStackTrace();
									}
								}
							});
							
							
						} catch(SocketException ex) {
							myShell.getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									showMessage("Submission failed!");
								}
							});
							//ex.printStackTrace();
						} catch (IOException ex) {
							myShell.getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									showMessage("Submission failed!");
								}
							});
							//ex.printStackTrace();
						} finally {
							try {
								if (com != null) {
									com.disconnect();
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}.start();
			}
			
		});

		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {

			@Override
			public void resourceChanged(IResourceChangeEvent event) {
				myShell.getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						buildCombo(assignmentTypeCombo.getText());
					}
					
				});
			}
			
		}, IResourceChangeEvent.POST_CHANGE);
		
		assignmentTypeCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				buildCombo(assignmentTypeCombo.getText());
				assignmentsCombo.select(0);
			}
		});
		
		ISecurePreferences root = SecurePreferencesFactory.getDefault();
		final ISecurePreferences node = root.node("/com/unc");
		
		courseCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				storePreference(node, "course", courseCombo.getItem(courseCombo.getSelectionIndex()), true);
			}
		});
		
		onyenText.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				storePreference(node, "onyen", onyenText.getText(), true);
			}
			
		});
		
		onyenText.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.LF) {
					storePreference(node, "onyen", onyenText.getText(), true);
				}
			}
			
		});
		
		passwordText.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				storePreference(node, "password", passwordText.getText(), true);
			}
			
		});
		passwordText.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.LF) {
					storePreference(node, "password", passwordText.getText(), true);
				}
			}
			
		});
	}
	
	private String readPreference(ISecurePreferences node, String name, String defaultValue) {
		try {
			Object pref = node.get(name, defaultValue);
			if(pref instanceof String) {
				return (String)pref;
			} else {
				return defaultValue;
			}
		} catch (StorageException e1) {
			e1.printStackTrace();
			return defaultValue;
		}
	}
	
	private void storePreference(ISecurePreferences node, String name, String value, boolean encrypted) {
		try {
			node.put(name, value, encrypted);
		} catch (StorageException e1) {
			e1.printStackTrace();
		}
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(myShell,
				"Assignment Grader", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		assignmentsCombo.setFocus();
	}
	
	private void buildCombo(String type) {
		String prevSel = null;
		try {
			prevSel = assignmentsCombo.getItem(assignmentsCombo.getSelectionIndex());
		} catch (IllegalArgumentException e) {
			
		}

		ArrayList<String> tmpAssignmentList = new ArrayList<String>();
		ArrayList<IProject> tmpProjectList = new ArrayList<IProject>();
		
		AssignmentFinder assignments = new AssignmentFinder(type);
		LinkedHashMap<String, IProject> map = assignments.getMap();
		
		for(String s : map.keySet()) {
			tmpAssignmentList.add(s);
			tmpProjectList.add(map.get(s));
		}
		
		int loc = tmpAssignmentList.indexOf(prevSel);
		if (loc < 0) {
			assignmentsCombo.select(0);
		} else {
			assignmentsCombo.select(loc);
		}
		
		assignmentProjects = tmpProjectList.toArray(new IProject[tmpProjectList.size()]);
		assignmentsCombo.setItems(tmpAssignmentList.toArray(new String[tmpAssignmentList.size()]));
	}
}