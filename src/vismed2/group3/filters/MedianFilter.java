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
	private int sliceYZ = 0;
	private int sliceXZ = 0;
	private int sliceAlong_Z = 0;

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
		/*
		 * for (int slice = filter_depth; slice < dims[2] - filter_depth;
		 * slice++) { for (int width = filter_width; width < dims[1] -
		 * filter_width; width++) { for (int height = filter_height; height <
		 * dims[0] - filter_height; height++) {
		 * 
		 * // fill values into kernel int count = 0; for (int y = slice -
		 * filter_depth; y < slice; y++) { for (int x = width - filter_width; x
		 * < width; x++) { for (int z = height - filter_height; z < height; z++)
		 * { pixelValue = imgData.GetScalarComponentAsDouble(z, x, y, 0);
		 * kernel[count++] = pixelValue; } } } // get medial by sorting the list
		 * of values and taking the // one in the middle Arrays.sort(kernel);
		 * pixelValue = kernel[kernel.length / 2 + 1];
		 * 
		 * out.SetScalarComponentFromDouble(height, width, slice, 0, pixelValue
		 * * -1); } } }
		 */

		// do filter for the pannel0 YZ
		for (int width = 0; width < dims[1]; width++) {
			for (int height = 0; height < dims[0]; height++) {

				// fill values into kernel
				int count = 0;
				for (int y = sliceYZ - filter_depth; y < sliceYZ; y++) {
					for (int x = width - filter_width; x < width; x++) {
						for (int z = height - filter_height; z < height; z++) {
							// clipping
							if (y < 0) { // kernel < first Slice
								pixelValue = imgData.GetScalarComponentAsDouble(z, x, 0, 0);
							} else if (y > dims[1]) { // kernel > last Slice
								int lastSlice = sliceYZ - (sliceYZ - dims[2]);
								pixelValue = imgData.GetScalarComponentAsDouble(z, x, lastSlice, 0);
							} else {
								pixelValue = imgData.GetScalarComponentAsDouble(z, x, y, 0);
							}
							kernel[count++] = pixelValue;
						}
					}
				}
				// get medial by sorting the list of values and taking the
				// one in the middle
				Arrays.sort(kernel);
				pixelValue = kernel[kernel.length / 2];

				out.SetScalarComponentFromDouble(height, width, sliceYZ, 0, pixelValue);
			}
		}
		
		// do filter for the pannel1 XZ
		for (int width = 0; width < dims[2]; width++) {
			for (int height = 0; height < dims[0]; height++) {
				pixelValue = imgData.GetScalarComponentAsDouble(height, width, sliceXZ, 0);

				// fill values into kernel
				int count = 0;
				for (int y = sliceXZ - filter_depth; y < sliceXZ; y++) {
					for (int x = width - filter_width; x < width; x++) {
						for (int z = height - filter_height; z < height; z++) {
							// clipping
							if (y < 0) { // kernel < first Slice
								pixelValue = imgData.GetScalarComponentAsDouble(z, 0, x, 0);
							} else if (y > dims[1]) { // kernel > last Slice
								int lastSlice = sliceXZ - (sliceXZ - dims[1]);
								pixelValue = imgData.GetScalarComponentAsDouble(z, lastSlice, x, 0);
							} else {
								pixelValue = imgData.GetScalarComponentAsDouble(z, y, x, 0);
							}
							kernel[count++] = pixelValue;
						}
					}
				}
				// get medial by sorting the list of values and taking the one
				// in the middle
				Arrays.sort(kernel);
				pixelValue = kernel[kernel.length / 2];

				out.SetScalarComponentFromDouble(height, sliceXZ, width, 0, pixelValue);
			}
		} 
		
		// do filter for the pannel1 YX
		for (int width = 0; width < dims[1]; width++) {
			for (int height = 0; height < dims[0]; height++) {
				pixelValue = imgData.GetScalarComponentAsDouble(height, width, sliceAlong_Z, 0);

				// fill values into kernel
				int count = 0;
				for (int y = sliceAlong_Z - filter_depth; y < sliceAlong_Z; y++) {
					for (int x = width - filter_width; x < width; x++) {
						for (int z = height - filter_height; z < height; z++) {
							// clipping
							if (y < 0) { // kernel < first Slice
								pixelValue = imgData.GetScalarComponentAsDouble(y, z, 0, 0);
							} else if (y > dims[1]) { // kernel > last Slice
								int lastSlice = sliceAlong_Z - (sliceAlong_Z - dims[2]);
								pixelValue = imgData.GetScalarComponentAsDouble(y, z, lastSlice, 0);
							} else {
								pixelValue = imgData.GetScalarComponentAsDouble(y, z, x, 0);
								//					 GetScalarComponentAsDouble(sliceYX, height, width, 0);
							}
							kernel[count++] = pixelValue;
						}
					}
				}
				// get median by sorting the list of values and taking the one
				// in the middle
				Arrays.sort(kernel);
				pixelValue = kernel[kernel.length / 2];

				out.SetScalarComponentFromDouble(sliceAlong_Z, height, width, 0, pixelValue);
			}
		}

	}

	public void setSlice(int sliceYZ, int sliceXZ, int sliceYX) {
		this.sliceYZ = sliceYZ;
		this.sliceXZ = sliceXZ;
		this.sliceAlong_Z = sliceYX;
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
