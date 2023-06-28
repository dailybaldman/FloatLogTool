package FloatLogTool;

public class MessageObject {
    int tag;
	String data;

	public MessageObject(int tag, String data) {
		this.tag = tag;
		this.data = data;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public int getTag() {
		return tag;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}
    
    
}
