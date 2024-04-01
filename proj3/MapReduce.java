import java.io.IOException;
import java.util.StringTokenizer;

import javax.naming.Context;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class MapReduce {

  private static final int IDENT = 0;
  private static final int TYPE = 1;
  private static final int NAME = 2;
  private static final int YEAR = 3;
  private static final int RATING = 4;
  private static final int GENRE_LIST = 5;

  private static final String MOVIE = "movie";
  private static final String RATING_CUTOFF = "7.5";
  private static final int FIRST_YEAR = 1991;
  private static final int LAST_YEAR = 2020;
  private static final int YEARS_IN_A_RANGE = 10;

  private static final String GENRE1A = "Action";
  private static final String GENRE1B = "Thriller";
  private static final String GENRE2A = "Adventure";
  private static final String GENRE2B = "Drama";
  private static final String GENRE3A = "Comedy";
  private static final String GENRE3B = "Romance";

  public static class TokenizerMapper
      extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private Text category = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      FileSplit fileSplit = (FileSplit) context.getInputSplit();
      String filename = fileSplit.getPath().getName();

      StringTokenizer itr = new StringTokenizer(value.toString(), "\n");
      while (itr.hasMoreTokens()) {
        String[] line = itr.nextToken().split(";");
        if (line.length <= GENRE_LIST || !line[TYPE].equals(MOVIE) || line[RATING].compareTo(RATING_CUTOFF) < 0) {
          break;
        }
        int year = 0;
        try {
          year = Integer.parseInt(line[YEAR]);
        } catch (Exception e) {}
        if (year < FIRST_YEAR || year > LAST_YEAR) {
          break;
        }
        int rangeStart = year - (year-1) % YEARS_IN_A_RANGE;
        int rangeEnd = rangeStart + 9;
        String yearRange = "[" + rangeStart + "-" + rangeEnd + "]";

        if (line[GENRE_LIST].contains(GENRE1A) && line[GENRE_LIST].contains(GENRE1B)) {
          category.set(yearRange + "," + GENRE1A + ";" + GENRE1B);
          context.write(category, one);
        }
        if (line[GENRE_LIST].contains(GENRE2A) && line[GENRE_LIST].contains(GENRE2B)) {
          category.set(yearRange + "," + GENRE2A + ";" + GENRE2B);
          context.write(category, one);
        }
        if (line[GENRE_LIST].contains(GENRE3A) && line[GENRE_LIST].contains(GENRE3B)) {
          category.set(yearRange + "," + GENRE3A + ";" + GENRE3B);
          context.write(category, one);
        }
      }
    }
  }

  public static class IntSumReducer
      extends Reducer<Text, IntWritable, Text, IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
        Context context) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(MapReduce.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
