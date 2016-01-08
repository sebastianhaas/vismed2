package vismed2.group3.dicom;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.UIDUtils;

import vtk.vtkDICOMImageReader;
import vtk.vtkImageData;
import vtk.vtkNativeLibrary;

public class DicomExporter {

	private Attributes dataset;

	// -----------------------------------------------------------------
	// Load VTK library and print which library was not properly loaded
	static {
		if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
			for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
				if (!lib.IsLoaded()) {
					System.out.println(lib.GetLibraryName() + " not loaded");
				}
			}
		}
		vtkNativeLibrary.DisableOutputWindow(null);
	}

	// -----------------------------------------------------------------

	public DicomExporter(vtkImageData imgData) {
		try {
			// Create and fill attributes for modules
			dataset = createDataset();
			setPatientModule();
			setGeneralStudyModule();
			setGeneralSeriesModule();
			setSCEquipmentModule();

			int[] dims = imgData.GetDimensions();
			for (int z = 0; z < dims[2]; z++) {
				
				// Append instance-specific attributes and create file meta information
				String instanceId = String.format("%04d", z);
				setGeneralImageModule(instanceId);
				setImagePixelModule(imgData);
				Attributes fileMetaInformation = dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian);
				
				// Open file stream
				DicomOutputStream out = new DicomOutputStream(new File("data/output/test-" + instanceId + ".dcm"));
				out.writeDataset(fileMetaInformation, dataset);

				// Prepare pixel data buffer and normalizing
				int sliceSize = (dims[0] * dims[1] * Short.SIZE) / 8;
				out.writeHeader(Tag.PixelData, VR.OW, sliceSize);
				double[] range = imgData.GetScalarRange();
				short vtkOffset = (short) Math.abs(range[0]);
				byte[] buffer = new byte[sliceSize];
				int i = 0;

				// Iterate over the image and store values in the buffer
				for (int y = dims[1] - 1; y >= 0; y--) {
					for (int x = 0; x < dims[0]; x++) {
						short val = (short) (imgData.GetScalarComponentAsDouble(x, y, z, 0) + vtkOffset);
						buffer[i] = (byte) (val & 0xff);
						buffer[i + 1] = (byte) ((val >> 8) & 0xff);
						i = i + 2;
					}
				}

				// Write buffer and close file stream
				out.write(buffer, 0, sliceSize);
				out.finish();
				out.close();
				
				System.out.println(String.format("Wrote slice %d/%d to DICOM file.", z+1, dims[2]));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// Get DICOM image data
		vtkDICOMImageReader dicomReader = new vtkDICOMImageReader();
		File directory = new File("data/Dentascan-0.75-H60s-3");
		dicomReader.SetDirectoryName(directory.getAbsolutePath()); // Spaces in
																	// path
																	// causing
																	// troubles
		dicomReader.Update();
		new DicomExporter(dicomReader.GetOutput());
	}

	private Attributes createDataset() {
		Attributes ds = new Attributes();
		ds.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
		return ds;
	}

	/**
	 * C.7.6.3 Image Pixel Module
	 */
	public void setImagePixelModule(vtkImageData imageData) {
		int[] dims = imageData.GetDimensions();
		double[] range = imageData.GetScalarRange();

		dataset.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
		dataset.setInt(Tag.PixelRepresentation, VR.US, 0);
		dataset.setInt(Tag.SamplesPerPixel, VR.US, 1);

		dataset.setInt(Tag.BitsAllocated, VR.US, 16);
		dataset.setInt(Tag.BitsStored, VR.US, 12);
		dataset.setInt(Tag.HighBit, VR.US, 11);

		dataset.setInt(Tag.Columns, VR.US, dims[0]);
		dataset.setInt(Tag.Rows, VR.US, dims[1]);
		dataset.setInt(Tag.SmallestImagePixelValue, VR.US, 0);
		dataset.setInt(Tag.LargestImagePixelValue, VR.US, (short) (range[1] + Math.abs(range[0])));
	}

	/**
	 * C.7.1.1 Patient Module
	 */
	public void setPatientModule() {
		dataset.setString(Tag.PatientName, VR.PN, "Doe^John");
		dataset.setString(Tag.PatientID, VR.LO, "PID00001234M");
		dataset.setString(Tag.PatientSex, VR.CS, "M");
		dataset.setDate(Tag.PatientBirthDate, VR.DA, new Date());
		dataset.setNull(Tag.ReferencedPatientSequence, VR.SQ);
	}

	/**
	 * C.7.2.1 General Study Module
	 */
	public void setGeneralStudyModule() {
		Date now = new Date();
		dataset.setString(Tag.StudyInstanceUID, VR.UI, UIDUtils.createUID());
		dataset.setDate(Tag.StudyDate, VR.DA, now);
		dataset.setDate(Tag.StudyTime, VR.TM, now);
		dataset.setString(Tag.ReferringPhysicianName, VR.CS, "Mister Physician");
		dataset.setString(Tag.StudyID, VR.SH, "123456789");
		dataset.setString(Tag.AccessionNumber, VR.SH, "0");
		dataset.setString(Tag.SOPClassUID, VR.UI, UID.CTImageStorage);
	}

	/**
	 * C.7.3.1 General Series Module
	 */
	public void setGeneralSeriesModule() {
		Date now = new Date();
		// C.7.3.1.1.1 Modality
		dataset.setString(Tag.Modality, VR.CS, "CT");
		dataset.setString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
		dataset.setString(Tag.SeriesNumber, VR.SH, "12345");
		dataset.setDate(Tag.SeriesDate, VR.DA, now);
		dataset.setDate(Tag.SeriesTime, VR.TM, now);
		dataset.setString(Tag.PerformingPhysicianName, VR.CS, "House");
	}

	/**
	 * C.8.6.1 SC Equipment Module
	 */
	public void setSCEquipmentModule() {
		dataset.setString(Tag.Modality, VR.CS, "CT"); // Actually redundant ->
														// series module
		dataset.setString(Tag.ConversionType, VR.CS, "WSD");
	}

	/**
	 * C.7.6.1 General Image Module
	 */
	public void setGeneralImageModule(String instanceNumber) {
		Date now = new Date();
		dataset.setString(Tag.InstanceNumber, VR.SH, instanceNumber);
		dataset.setDate(Tag.ContentDate, VR.DA, now);
		dataset.setDate(Tag.ContentTime, VR.DA, now);
		dataset.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
	}
}
