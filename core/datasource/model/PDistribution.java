package core.datasource.model;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("flicka")
@Table("pdistribution")
@CompositePK({ "pdrace", "pdfield", "pdvalue", "pddate" })
public class PDistribution extends Model {

}
