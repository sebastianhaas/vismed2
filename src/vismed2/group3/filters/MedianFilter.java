package vismed2.group3.filters;

import java.util.Arrays;

import vtk.vtkImageData;

/**
 * The kernel- size has to be an uneven number!
 */
public class MedianFilter implements VtkJavaFilter {

	private final vtkImageData out;

	private int filter_height;
	private int filter_width;
	private int filter_depth;

	public MedianFilter() {
		out = new vtkImageData();
	}

	public void SetKernelSize(int height, int width, int depth) {
		this.filter_height = height;
		this.filter_width = width;
		this.filter_depth = depth;
	}

	/**
	 * The kernel- size has to be an uneven number!
	 */
	@Override
	public void applyFilter(vtkImageData imgData) {
		// Prepare output data
		int[] dims = imgData.GetDimensions();
		out.Initialize();
		out.CopyStructure(imgData);
		out.CopyAttributes(imgData);
		out.DeepCopy(imgData);

		// // Anzahl der Komponenten pro Skalar, bei einem DICOM Bild wird das 1
		// // sein d.h. eh nur b/w
		// int num = imgData.GetNumberOfScalarComponents();
		// // Größe des skalar typs in Bytes. Hier short, also 2.
		// int size = imgData.GetScalarSize();
		// // Bereich der vorkommenden Skalarwerte von -irgendwas bis +irgendwas
		// in einem 2dim array
		// double[] range = imgData.GetScalarRange();
		// // Skalar typ als String, bei DICOM ists ein short
		// String type = imgData.GetScalarTypeAsString();
		// // Minimal erlaubter Wert im Skalar für den gegebenen Datentyp, z.B.
		// bei mir -32768.0 weil short
		// double min = imgData.GetScalarTypeMin();
		// // Minimal erlaubter Wert im Skalar für den gegebenen Datentyp, z.B.
		// bei mir 32768.0 weil short
		// double max = imgData.GetScalarTypeMax();
		// // Typ des Skalars als int, hab die enumeration nicht gefunden
		// dafür,
		// // aber short ist offenbar 4 ;)
		// int typeInt = imgData.GetScalarType();

		// do actual filtering
		double[] kernel = new double[filter_height * filter_width * filter_depth];
		double pixelValue;

		// iterate through the image/ through all slices
		for (int slice = filter_depth; slice < dims[2] - filter_depth; slice++) {
			for (int width = filter_width; width < dims[1] - filter_width; width++) {
				for (int height = filter_height; height < dims[0] - filter_height; height++) {

					// fill values into kernel
					int count = 0;
					for (int y = slice - filter_depth; y < slice; y++) {
						for (int x = width - filter_width; x < width; x++) {
							for (int z = height - filter_height; z < height; z++) {
								pixelValue = imgData.GetScalarComponentAsDouble(z, x, y, 0);
								kernel[count++] = pixelValue;
							}
						}
					}
					// get medial by sorting the list of values and taking the
					// one in the middle
					Arrays.sort(kernel);
					pixelValue = kernel[kernel.length / 2 + 1];

					out.SetScalarComponentFromDouble(height, width, slice, 0, pixelValue * -1);
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
