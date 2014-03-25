package travel.kiri.backend.algorithm;

public interface MemorySize 
{
	int BYTE_SIZE = 1;
	int SHORT_SIZE = 2;
	int INT_SIZE = 4;
	int LONG_SIZE = 8;
	int FLOAT_SIZE = 4;
	int DOUBLE_SIZE = 8;
	int BOOLEAN_SIZE = 1;
	int CHAR_SIZE = 2;
		
	int getMemorySize();
}
