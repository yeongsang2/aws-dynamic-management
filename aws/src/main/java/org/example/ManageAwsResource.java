package org.example;

/*
 * Cloud Computing
 *
 * Dynamic Resource Management Tool
 * using AWS Java SDK Library
 *
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Scanner;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;


public class ManageAwsResource {

    static AmazonEC2 ec2;

    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    private static void init() throws Exception {

        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("ap-southeast-2")	/* check the region at AWS console */
                .build();
    }

    public static void main(String[] args) throws Exception {

        init();

        Scanner menu = new Scanner(System.in);
        Scanner id_string = new Scanner(System.in);
        int number = 0;

        while(true)
        {
            System.out.println("                                                            ");
            System.out.println("                                                            ");
            System.out.println("------------------------------------------------------------");
            System.out.println("           Amazon AWS Control Panel using SDK               ");
            System.out.println("------------------------------------------------------------");
            System.out.println("  1. list instance                2. available zones        ");
            System.out.println("  3. start instance               4. available regions      ");
            System.out.println("  5. stop instance                6. create instance        ");
            System.out.println("  7. reboot instance              8. list images            ");
            System.out.println("  9. create key pair              10. list Key Pair         ");
            System.out.println("  11. delete key pair             12. condor status         ");
            System.out.println("                                 99. quit                   ");
            System.out.println("------------------------------------------------------------");

            System.out.print("Enter an integer: ");

            if(menu.hasNextInt()){
                number = menu.nextInt();
            }else {
                System.out.println("concentration!");
                break;
            }


            String instance_id = "";

            switch(number) {
                case 1:
                    listInstances();
                    break;

                case 2:
                    availableZones();
                    break;

                case 3:
                    System.out.print("Enter instance id: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.isBlank())
                        startInstance(instance_id);
                    break;

                case 4:
                    availableRegions();
                    break;

                case 5:
                    System.out.print("Enter instance id: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.isBlank())
                        stopInstance(instance_id);
                    break;

                case 6:
                    System.out.print("Enter ami id: ");
                    String ami_id = "";
                    if(id_string.hasNext())
                        ami_id = id_string.nextLine();

                    if(!ami_id.isBlank())
                        createInstance(ami_id);
                    break;

                case 7:
                    System.out.print("Enter instance id: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.isBlank())
                        rebootInstance(instance_id);
                    break;

                case 8:
                    listImages();
                    break;

                case 9:
                    createKeyPair();
                    break;

                case 10:
                    listKeyPairs();
                    break;

                case 11:
                    deleteKeyPairs();
                    break;

                case 12:
                    condorStatus();
                    break;

                case 99:
                    System.out.println("bye!");
                    menu.close();
                    id_string.close();
                    return;
                default: System.out.println("concentration!");
            }

        }

    }

    private static void condorStatus() throws IOException {

        //http
        String condorStatus = RequestCondorStatus.getCondorStatus();

        for (int i = 0; i < condorStatus.length(); i++) {

            if( (i % 109 ) == 0){
                System.out.println();
            }
            System.out.print(condorStatus.charAt(i));

        }

    }

    public static void listInstances() {

        System.out.println("Listing instances....");
        boolean done = false;

        DescribeInstancesRequest request = new DescribeInstancesRequest();

        while(!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);

            for(Reservation reservation : response.getReservations()) {
                for(Instance instance : reservation.getInstances()) {
                    System.out.printf(
                            "[id] %s, " +
                                    "[AMI] %s, " +
                                    "[type] %s, " +
                                    "[state] %10s, " +
                                    "[monitoring state] %s",
                            instance.getInstanceId(),
                            instance.getImageId(),
                            instance.getInstanceType(),
                            instance.getState().getName(),
                            instance.getMonitoring().getState());
                }
                System.out.println();
            }

            request.setNextToken(response.getNextToken());

            if(response.getNextToken() == null) {
                done = true;
            }
        }
    }

    public static void availableZones()	{

        System.out.println("Available zones....");
        try {
            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            Iterator <AvailabilityZone> iterator = availabilityZonesResult.getAvailabilityZones().iterator();

            AvailabilityZone zone;
            while(iterator.hasNext()) {
                zone = iterator.next();
                System.out.printf("[id] %s,  [region] %15s, [zone] %15s\n", zone.getZoneId(), zone.getRegionName(), zone.getZoneName());
            }
            System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
                    " Availability Zones.");

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }

    }

    public static void startInstance(String instance_id)
    {

        System.out.printf("Starting .... %s\n", instance_id);
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StartInstancesRequest> dry_request =
                () -> {
                    StartInstancesRequest request = new StartInstancesRequest()
                            .withInstanceIds(instance_id);

                    return request.getDryRunRequest();
                };

        StartInstancesRequest request = new StartInstancesRequest()
                .withInstanceIds(instance_id);

        ec2.startInstances(request);

        System.out.printf("Successfully started instance %s", instance_id);
    }


    public static void availableRegions() {

        System.out.println("Available regions ....");

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeRegionsResult regions_response = ec2.describeRegions();

        for(Region region : regions_response.getRegions()) {
            System.out.printf(
                    "[region] %15s, " +
                            "[endpoint] %s\n",
                    region.getRegionName(),
                    region.getEndpoint());
        }
    }

    public static void stopInstance(String instance_id) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StopInstancesRequest> dry_request =
                () -> {
                    StopInstancesRequest request = new StopInstancesRequest()
                            .withInstanceIds(instance_id);

                    return request.getDryRunRequest();
                };

        try {
            StopInstancesRequest request = new StopInstancesRequest()
                    .withInstanceIds(instance_id);

            ec2.stopInstances(request);
            System.out.printf("Successfully stop instance %s\n", instance_id);

        } catch(Exception e)
        {
            System.out.println("Exception: "+e.toString());
        }

    }

    public static void createInstance(String ami_id) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        RunInstancesRequest run_request = new RunInstancesRequest()
                .withImageId(ami_id)
                .withInstanceType(InstanceType.T2Micro)
                .withMaxCount(1)
                .withMinCount(1);

        RunInstancesResult run_response = ec2.runInstances(run_request);

        String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

        System.out.printf(
                "Successfully started EC2 instance %s based on AMI %s",
                reservation_id, ami_id);

    }

    public static void rebootInstance(String instance_id) {

        System.out.printf("Rebooting .... %s\n", instance_id);

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        try {
            RebootInstancesRequest request = new RebootInstancesRequest()
                    .withInstanceIds(instance_id);

            RebootInstancesResult response = ec2.rebootInstances(request);

            System.out.printf(
                    "Successfully rebooted instance %s", instance_id);

        } catch(Exception e)
        {
            System.out.println("Exception: "+e.toString());
        }


    }

    public static void listImages() {
        System.out.println("Listing images....");

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeImagesRequest request = new DescribeImagesRequest();
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();

        request.getFilters().add(new Filter().withName("name").withValues("aws-htcondor-slave"));
        request.setRequestCredentialsProvider(credentialsProvider);

        DescribeImagesResult results = ec2.describeImages(request);

        for(Image images :results.getImages()){
            System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n",
                    images.getImageId(), images.getName(), images.getOwnerId());
        }

    }

    public static void createKeyPair() throws IOException {
        final String USAGE =
                "supply a key pair name which you want to create: ";

        System.out.print(USAGE);

        String key_name = br.readLine();

        if(!checkDuplicateKeyName(key_name)) {
            System.out.println();
            System.out.print("Key name already exists, you should use another key name");
            return;
        }

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        CreateKeyPairRequest request = new CreateKeyPairRequest()
                .withKeyName(key_name);

        CreateKeyPairResult response = ec2.createKeyPair(request);

        System.out.println(response);

        System.out.printf(
                "Successfully created key pair named %s",
                key_name);
    }

    public static void listKeyPairs(){
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeKeyPairsResult response = ec2.describeKeyPairs();

        for(KeyPairInfo key_pair : response.getKeyPairs()) {
            System.out.printf(
                    "Found key pair with name %s " +
                            "and fingerprint %s",
                    key_pair.getKeyName(),
                    key_pair.getKeyFingerprint());
            System.out.println();
        }
    }

    public static void deleteKeyPairs() throws IOException {
        final String USAGE =
                "supply a key pair name which you want to delete: ";
        System.out.print(USAGE);
        String key_name = br.readLine();

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DeleteKeyPairRequest request = new DeleteKeyPairRequest()
                .withKeyName(key_name);

        System.out.printf(
                "Successfully deleted key pair named %s", key_name);
    }

    public static boolean checkDuplicateKeyName(String keyName){

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeKeyPairsResult response = ec2.describeKeyPairs();
        for(KeyPairInfo key_pair : response.getKeyPairs()) {
            if(keyName.equals(key_pair.getKeyName())){
                return false;
            }
        }
        return true;

    }

}
