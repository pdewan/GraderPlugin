package com.unc.cs.graderprogramplugin.utils;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Andrew Vitkus
 *
 */
public class AssignmentFinder {
	private String type;
	
	public AssignmentFinder(String type) {
		this.type = type;
	}
	
	public LinkedHashMap<String, IProject> getMap() {
		ArrayList<String> strings = new ArrayList<String>();
		ArrayList<IProject> projects = new ArrayList<IProject>();
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		for(IProject p : myWorkspaceRoot.getProjects()) {
			String name;
			try {
				name = getAssignmentName(p);
				if (name != null) {
					projects.add(p);
					strings.add(name);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		return projectArrSort(strings, projects);
	}
	
	private String getAssignmentName(IResource r) throws CoreException {
		switch (r.getType()) {
			case IResource.FILE:
			{
				if (r.isAccessible() && r.getName().matches(type + "[ ]*[0-9]+.java")) {
					String num = r.getName().substring(type.length(), r.getName().length() - 5);
					String name = r.getProject().getName() + " (" + type + " " + num + ")";
					return name;
				} else {
					return null;
				}
			}
			case IResource.PROJECT:
			{
				IProject p = (IProject)r;
				if (p.isAccessible()) {
					for(IResource child : p.members()) {
						String name = getAssignmentName(child);
						if (name != null) {
							return name;
						}
					}
				}
				break;
			}
			case IResource.FOLDER:
			{
				IFolder f = (IFolder)r;
				if (f.isAccessible()) {
					for(IResource child : f.members()) {
						String name = getAssignmentName(child);
						if (name != null) {
							return name;
						}
					}
				}
				break;
			}
		}
		return null;
	}
	
	private LinkedHashMap<String, IProject> projectArrSort(List<String> strings, List<IProject> projects) {
		for(int end = strings.size() - 1; end > 0; end --) {
			for(int i = 0; i < end; i ++) {
				String strA = strings.get(i);
				int numA = Integer.parseInt(strA.substring(strA.lastIndexOf(' ') + 1, strA.length() - 1));
				String strB = strings.get(i + 1);
				int numB = Integer.parseInt(strB.substring(strB.lastIndexOf(' ') + 1, strB.length() - 1));
				if (numA < numB) {
					String tmpStr = strings.get(i + 1);
					strings.set(i + 1, strings.get(i));
					strings.set(i, tmpStr);
					
					IProject tmpProj = projects.get(i + 1);
					projects.set(i + 1, projects.get(i));
					projects.set(i, tmpProj);
				}
			}
		}
		
		LinkedHashMap<String, IProject> map = new LinkedHashMap<String, IProject>(strings.size());
		for(int i = 0; i < strings.size(); i ++) {
			map.put(strings.get(i), projects.get(i));
		}
		
		return map;
	}
}
