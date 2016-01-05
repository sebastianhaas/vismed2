package vismed2.group3.filters;

import vtk.vtkImageData;

public class MedianFilter implements VtkJavaFilter {
	
	private final vtkImageData out;
	
	public MedianFilter() {
		out = new vtkImageData();
	}
	
	public void SetKernelSize() {
	}

	@Override
	public void applyFilter(vtkImageData imgData) {
		// Prepare output data
		int[] dims = imgData.GetDimensions();
		out.Initialize();
		out.CopyStructure(imgData);
		out.CopyAttributes(imgData);
		out.DeepCopy(imgData);

//		// Anzahl der Komponenten pro Skalar, bei einem DICOM Bild wird das 1
//		// sein d.h. eh nur b/w
//		int num = imgData.GetNumberOfScalarComponents();
//		// Größe des skalar typs in Bytes. Hier short, also 2.
//		int size = imgData.GetScalarSize();
//		// Bereich der vorkommenden Skalarwerte von -irgendwas bis +irgendwas in einem 2dim array
//		double[] range = imgData.GetScalarRange();
//		// Skalar typ als String, bei DICOM ists ein short
//		String type = imgData.GetScalarTypeAsString();
//		// Minimal erlaubter Wert im Skalar für den gegebenen Datentyp, z.B. bei mir -32768.0 weil short
//		double min = imgData.GetScalarTypeMin();
//		// Minimal erlaubter Wert im Skalar für den gegebenen Datentyp, z.B. bei mir 32768.0 weil short
//		double max = imgData.GetScalarTypeMax();
//		// Typ des Skalars als int, hab die enumeration nicht gefunden dafür,
//		// aber short ist offenbar 4 ;)
//		int typeInt = imgData.GetScalarType();
		
		
		
		for (int z = 0; z < dims[2]; z++) {
			for (int y = 0; y < dims[1]; y++) {
				for (int x = 0; x < dims[0]; x++) {
					double pixelValue = imgData.GetScalarComponentAsDouble(x, y, z, 0);
					out.SetScalarComponentFromDouble(x, y, z, 0, pixelValue * -1);
				}
			}
		}
	}

	@Override
	public String getFilterName() {
		return "Median";
	}

	@Override
	public vtkImageData GetOutput() {
		return out;
	}
}
