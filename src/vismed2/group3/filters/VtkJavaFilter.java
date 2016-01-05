package vismed2.group3.filters;

import vtk.vtkImageData;

public interface VtkJavaFilter {

	public void applyFilter(vtkImageData imgData);
	
	public String getFilterName();
	
	public vtkImageData GetOutput();
}
