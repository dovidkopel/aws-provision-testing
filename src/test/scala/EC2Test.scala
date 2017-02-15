import com.monsanto.arch.cloudformation.model._
import spray.json._
import DefaultJsonProtocol._
import com.monsanto.arch.cloudformation.model.resource._
import com.monsanto.arch.cloudformation.model.simple.Builders
import com.monsanto.arch.cloudformation.model.simple.Builders._

/**
  * Created by dkopel on 2/15/17.
  */
object EC2Test extends App {
    val vpcCidrParameter = CidrBlockParameter(
        name        = "VpcCidr",
        Description = Some("CIDR address range for the VPC to be created"),
        Default     = Some(CidrBlock(10,183,0,0,16))
    )

    val availabilityZone1Parameter = StringParameter.apply("us-east-1")
    val availabilityZone2Parameter = StringParameter.apply("us-east-2")

    val publicSubnet1CidrParameter = CidrBlockParameter(
        name        = "VpcCidr",
        Description = Some("CIDR address range for the VPC to be created"),
        Default     = Some(CidrBlock(10,183,0,0,16))
    )

    val keyNameParameter = `AWS::EC2::KeyPair::KeyName_Parameter`("KeyNamePair", None)

    val privateSubnet1CidrParameter = CidrBlockParameter(
        name        = "VpcCidr",
        Description = Some("CIDR address range for the VPC to be created"),
        Default     = Some(CidrBlock(10,183,0,0,16))
    )

    val amazonLinuxAMIMapping = Mapping("AWSRegionArch2AMI", Map(
        "us-east-1" -> Map(
            "t2.micro" -> AMIId("ami-2a69aa47"),
            "m1.small" -> AMIId("ami-2a69aa47")
        )
    ))

    val agsParam = StringParameter("AGS")
    val sdlcParam = StringParameter("SDLC")

    val vpc: Template = withVpc(ParameterRef(vpcCidrParameter)){ implicit vpc =>
        withAZ(ParameterRef(availabilityZone1Parameter)){ implicit az1 =>
            withSubnet("Public", ParameterRef(publicSubnet1CidrParameter)){ implicit pubSubnet1 =>
                ec2(
                    "myInstance",
                    InstanceType = "t2.micro",
                    KeyName = ParameterRef(keyNameParameter),
                    ImageId = `Fn::FindInMap`[AMIId](MappingRef(amazonLinuxAMIMapping), `AWS::Region`, "AMI"),
                    SecurityGroupIds = Seq(),
                    Tags = Seq(
                        AmazonTag("AGS", ParameterRef(agsParam)),
                        AmazonTag("SDLC", ParameterRef(sdlcParam))
                    ),
                    UserData = None
                )
            }
        }
    }

    val complete = Template(
        AWSTemplateFormatVersion = "2010-09-09",
        Description = "Simple vpc",
        Parameters = Some(
            Seq(
                vpcCidrParameter,
                agsParam,
                sdlcParam,
                availabilityZone1Parameter,
                availabilityZone2Parameter,
                publicSubnet1CidrParameter,
                keyNameParameter,
                privateSubnet1CidrParameter
            )
        ),
        Resources = vpc.Resources,
        Conditions = None,
        Mappings = None,
        Outputs = None
    )

    val out = complete.toJson.prettyPrint
    println(out)
}
