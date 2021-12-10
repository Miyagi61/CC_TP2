import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ByteManager {
        private List<byte[]> L_array;
        private int count;
        private int items;
        //creating a constructor of the class that initializes the values
        public ByteManager()
        {
            L_array = new ArrayList<>();
            count = 0;
            items = 0;
        }
        //creating a function that appends an element at the end of the array
        public void addElement(byte[] array)
        {
            L_array.add(array);
            count++;
        }

        public void addItem(){
            items++;
        }

        public int getCount(){
            return count;
        }

        public int getItems(){
            return items;
        }

        public byte[] getL_array(int idx){
            return L_array.get(idx);
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
