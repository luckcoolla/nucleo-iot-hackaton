#include "mbed.h"
  #include "SOFBlock.h"
char * a = "sample"; 
  
  int main()
  {
   const uint8_t sector_index = 7;
   //SOFBlock::format(sector_index); // Erase flash sector 7 and make structure for storage. 
   
   SOFWriter writer;
   SOFReader reader;
   
   reader.open(sector_index);
   printf("0 - data %d bytes at %p :\r\n", reader.get_data_size(), reader.get_physical_data_addr());
   printf("%.*s\r\n", reader.get_data_size(), reader.get_physical_data_addr());
   // "First Data" printed
   reader.close();     
 
   SOF_Statics_t statics;
   if (!SOFBlock::get_stat(sector_index, statics) || statics.free_size < 11) { // check available byte
      SOFBlock::format(sector_index);
   }
   writer.open(sector_index);
   writer.write_data((uint8_t*) a, 10);
   writer.close();
 
   reader.open(sector_index);
   printf("1 - data %d bytes at %p :\r\n", reader.get_data_size(), reader.get_physical_data_addr());
   printf("%.*s\r\n", reader.get_data_size(), reader.get_physical_data_addr());
   // "First Data" printed
   reader.close();
 
   SOF_Statics_t statics1;
   if (!SOFBlock::get_stat(sector_index, statics1) || statics1.free_size < 11) { // check available byte
      SOFBlock::format(sector_index);
   }
   writer.open(sector_index);
   // Overwrite previous data without erasing flash.
   writer.write_data((uint8_t*) "b", 1);
   writer.close();
 
   reader.open(sector_index);
   printf("2 - data %d bytes at %p :\r\n", reader.get_data_size(), reader.get_physical_data_addr());
   printf("%.*s\r\n", reader.get_data_size(), reader.get_physical_data_addr());
   // "Second Data" printed
   reader.close();
  }
