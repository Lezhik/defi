package defi;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.ColumnIOFactory;

@Slf4j
public class Importer {
    static final String F1 = "imported/part_00000_62c9c86c_8a10_4196_b54c_01a2a139f4ec_c000_snappy.parquet";
    static final String F2 = "imported/part_00000_32767f69_9150_49ac_9c03_45f34b103c34_c000_snappy.parquet";

    static final int NUMBER_FIELD = 4;

    public static void main(String[] args) throws Exception {
        System.setProperty("hadoop.home.dir", "d:\\devel\\tools\\hadoop-common-2.2.0");
        var reader = ParquetFileReader.open(HadoopInputFile.fromPath(new Path(F2), new Configuration()));
        var schema = reader.getFooter().getFileMetaData().getSchema();
        var fields = schema.getFields();
        var index = 0;
        for (var field: fields) {
            if (field.isPrimitive()) {
                log.info("Field name: {}, index: {}, type: {}", field.getName(), index, field.asPrimitiveType().getPrimitiveTypeName());
            } else {
                log.info("Field name: {}, index: {}, type: {}", field.getName(), index, field.getOriginalType().name());
            }
            index++;
        }
        var columnIO = new ColumnIOFactory().getColumnIO(schema);
        PageReadStore pages;
        while ((pages = reader.readNextRowGroup()) != null) {
            long rows = pages.getRowCount();
            var recordReader = columnIO.getRecordReader(pages, new GroupRecordConverter(schema));
            for (int i = 0; i < rows; i++) {
                var simpleGroup = (SimpleGroup) recordReader.read();
                var blockNum = simpleGroup.getLong(NUMBER_FIELD, 0);
                log.info("Block number: {}", blockNum);
            }
        }
        reader.close();
    }
}
