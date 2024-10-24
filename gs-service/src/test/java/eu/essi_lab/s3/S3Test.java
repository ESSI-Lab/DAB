//package eu.essi_lab.s3;
//
//import java.io.File;
//
//import com.amazonaws.AmazonServiceException;
//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//
//public class S3Test {
//    
//    public S3Test() {
//	System.out.format("Uploading %s to S3 bucket %s...\n", file_path, bucket_name);
//	final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
//	try {
//	    s3.putObject(bucket_name, key_name, new File(file_path));
//	} catch (AmazonServiceException e) {
//	    System.err.println(e.getErrorMessage());
//	    System.exit(1);
//    }
//    
//    public static void main(String[] args) {
//	S3Test test = new S3Test();
//	test.upload()
//    }
//}
