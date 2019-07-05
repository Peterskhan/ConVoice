package convoice.server.channel;


// Java imports
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * The ChannelSerializer class is responsible for providing
 * an XML compatible interface for serializing and deserializing 
 * multiple ChannelData objects to XML-format. 
 */
@XmlRootElement(name = "ChannelList")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChannelSerializer {
	/** The list of ChannelData objects being serialized/deserialized. */
	@XmlElement(name = "Channel")
	private List<ChannelData> m_data = null;	
	
	/**
	 * Constructs a ChannelSerializer object.
	 * The object is constructed by the user code upon serialization
	 * and is used by the XML Marshaller, while upon deserialization
	 * it is constructed by the XML Unmarshaller and queried for data
	 * by the user code.
	 */
	public ChannelSerializer() {
		// Initializing members
		m_data = new ArrayList<ChannelData>();
	}
	
	/**
	 * Gets the list of ChannelData objects.
	 * @return The list containing the serialized/deserialized data.
	 */
	public List<ChannelData> getData() {
		return m_data;
	}
	
	/**
	 * Sets the list of ChannelData objects. 
	 * @param channels The list containing the serialized/deserialized data.
	 */
	public void setData(List<ChannelData> channels) {
		m_data = channels;
	}

};
