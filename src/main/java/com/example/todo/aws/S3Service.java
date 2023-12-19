package com.example.todo.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.annotation.PostConstruct;

@Component // @Service로 설정해도 무관~
@Slf4j
public class S3Service {

    private S3Client s3;

    @Value("${aws.credentials.accessKey}")
    private String accessKey;

    @Value("${aws.credentials.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.bucketName}")
    private String bucketName;

    
    @PostConstruct // S3Service 객체가 생성될 때 한번만 실행되는 아노테이션 (객체가 생성될 때 자동으로 한번 호출되고 그 이후로는 불릴 일이 없음)
    // s3에 연결해서 인증을 처리하는 로직 (지금 s3bucekt-admin 이라는 계정으로 접근 시도하는 것)
    private void initializeAmazon() {
        // 액세스 키와 시크릿 키를 이용해서 계정 인증 받아서 AwsBasicCredentials 객체 받기 (이따 S3Client 빌드 시 필요함)
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        // 이 객체를 한번만 얻어내고나면 앞으로는 또 빌드 할 필요 없음 (@PostConstruct 사용)
        this.s3 = S3Client.builder()
                .region(Region.of(region)) // 매개변수로 Region 타입을 필요로 함
                .credentialsProvider((StaticCredentialsProvider.create(credentials)))
                .build();

    }

    /**
     * 버킷에 파일을 업로드하고. 업로드한 버킷의 url 정보를 리턴
     * @param uploadFile - 업로드 할 파일의 실제 raw 데이터 (가공하지 않은 순수한 객체 타입)
     * @param fileName - 업로드 할 파일명
     * @return - 버킷에 업로드 된 버킷 경로(url)
     */
    public String uploadToS3Bucket(byte[] uploadFile, String fileName) {

        // 업로드 할 파일을 S3 객체로 생성
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName) //버킷 이름
                .key(fileName) //파일명
                .build();

        // 생성한 오브젝트를 버킷에 업로드 매개값(위에서 생성한 오브젝트, 업로드 하고자 하는 파일(바이트 배열))
        s3.putObject(request, RequestBody.fromBytes(uploadFile));

        // 업로드 된 파일의 url을 반환
        return s3.utilities()
                .getUrl(builder -> builder.bucket(bucketName).key(fileName)) // 저장된 이미지의 url을
                .toString(); // String으로 변환



    }


}
