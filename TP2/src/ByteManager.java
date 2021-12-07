import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ByteManager {
        private List<byte[]> L_array;
        private int count;
        private int sizeofarray;
        private int byteSize;
        //creating a constructor of the class that initializes the values
        public ByteManager(int byteSize)
        {
            L_array = new ArrayList<>();
            count = 0;
            sizeofarray = 1;
            this.byteSize = byteSize;
        }
        //creating a function that appends an element at the end of the array
        public void addElement()
        {
            Random rs = new Random();
            byte[] array = new byte[this.byteSize];
            rs.nextBytes(array);
            L_array.add(array);
            count++;
        }
        static byte[] concatByteArray(byte[] cab, byte[] corpo){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            try{
                outputStream.write( cab );
                outputStream.write( corpo );
            }catch (IOException e){
                System.out.println(e.getMessage());
            }
            return outputStream.toByteArray( );
        }

}
