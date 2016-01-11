package vismed2.group3.dicom;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.UIDUtils;

import vtk.vtkImageData;

/**
 * An export-module to write {@link vtk.vtkImageData} to file using the DICOM
 * standard. Since this file format requires a lot of metadata, this class
 * generates and uses a set of dummy patient and study data. <br>
 * <br>
 * According to the DICOM standard, the required metadata modules for a series
 * of images from a study on a single patient are: <br>
 * C.7.1.1 Patient Module<br>
 * C.7.2.1 General Study Module<br>
 * C.7.3.1 General Series Module<br>
 * C.7.6.1 General Image Module<br>
 * C.7.6.3 Image Pixel Module<br>
 * C.8.6.1 SC Equipment Module<br>
 * <br>
 * While the Patient, Study, Series and Equipment modules are independent of the
 * actual instance, the Image Pixel Module is context-sensitive and is tied to
 * the actual pixel data in the current frame.
 * 
 * <h4>Metadata</h4> All of the required metadata will be generated by this
 * class using either random or static values, or by analyzing the given input
 * data. The patient name is set to John Doe, and the examination modality is a
 * standard CT scan.
 * 
 * <h4>Supported Image Format</h4> This export-module works for uncompressed,
 * monochrome image data only, such as retrieved by a regular CAT-scan. It will
 * adapt to different resolutions and therefore numbers of slices though.<br>
 * The incoming data is expected to consist of scalars of length 1 per pixel
 * (samples per pixel). Since CT data usually uses 4096 different brightness
 * levels, a short is used to store data. In DICOM, 16 bits are allocated with
 * 12 bits in use to store the short value. The high bit is 11 (MSB). Rows and
 * columns as well as the smallest and largest occurring pixel value are set
 * automatically depending on the currently processed instance.
 * 
 * 
 * @author Sebastian Haas
 * @author Alexander Tatowsky
 * 
 * @see <a href="http://dicom.nema.org/standard.html">http://dicom.nema.org</a>
 * @see <a href=
 *      "http://forums.dcm4che.org/jiveforums/message.jspa?messageID=10681">
 *      dcm4che forum entry on uncompressed data export</a>
 * @see <a href=
 *      "http://dicomiseasy.blogspot.co.at/2012/08/chapter-12-pixel-data.html">
 *      Blog entry on pixel data format in DICOM</a>
 * @see <a href=
 *      "http://dicomiseasy.blogspot.co.at/2011/12/chapter-4-dicom-objects-in-chapter-3.html">
 *      Blog entry on DICOM data structure</a>
 */
public class DicomExporter {

	private Attributes dataset;
	private ChangeListener listener;

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

	/**
	 * Can be used to register a {@link ChangeListener} which will be informed
	 * about the export's progress.
	 * 
	 * @param changeListener
	 *            The {@link ChangeListener} to register.
	 */
	public void setChangeListener(ChangeListener changeListener) {
		listener = changeListener;
	}

	/**
	 * Removes the registered {@link ChangeListener}, if any.
	 */
	public void removeChangeListener() {
		listener = null;
	}

	/**
	 * Exports the given image data to the location specified. Any previously
	 * registered {@link ChangeListener} will be informed about the progress.
	 * 
	 * @param imageData
	 *            The data to write to DICOM
	 * @param filePathAndBaseName
	 *            The location of the DICOM file(s) to write. Must be an
	 *            existing and writable location.
	 */
	public void exportImageData(vtkImageData imageData, String filePathAndBaseName) {
		try {
			// Create and fill attributes for modules
			dataset = createDataset();
			setPatientModule();
			setGeneralStudyModule();
			setGeneralSeriesModule();
			setSCEquipmentModule();

			int[] dims = imageData.GetDimensions();
			for (int z = 0; z < dims[2]; z++) {

				// Append instance-specific attributes and create file meta
				// information
				String instanceId = String.format("%04d", z);
				setGeneralImageModule(instanceId);
				setImagePixelModule(imageData);
				Attributes fileMetaInformation = dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian);

				// Open file stream
				DicomOutputStream out = new DicomOutputStream(
						new File(filePathAndBaseName + "-" + instanceId + ".dcm"));
				out.writeDataset(fileMetaInformation, dataset);

				// Prepare pixel data buffer and normalizing
				int sliceSize = (dims[0] * dims[1] * Short.SIZE) / 8;
				out.writeHeader(Tag.PixelData, VR.OW, sliceSize);
				double[] range = imageData.GetScalarRange();
				short vtkOffset = (short) Math.abs(range[0]);
				byte[] buffer = new byte[sliceSize];
				int i = 0;

				// Iterate over the image and store values in the buffer
				for (int y = dims[1] - 1; y >= 0; y--) {
					for (int x = 0; x < dims[0]; x++) {
						short val = (short) (imageData.GetScalarComponentAsDouble(x, y, z, 0) + vtkOffset);
						buffer[i] = (byte) (val & 0xff);
						buffer[i + 1] = (byte) ((val >> 8) & 0xff);
						i = i + 2;
					}
				}

				// Write buffer and close file stream
				out.write(buffer, 0, sliceSize);
				out.finish();
				out.close();

				System.out.println(String.format("Wrote slice %d/%d to DICOM file.", z + 1, dims[2]));
				if (listener != null) {
					listener.stateChanged(new ChangeEvent(z + 1));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
