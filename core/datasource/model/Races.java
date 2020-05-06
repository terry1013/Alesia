package core.datasource.model;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("flicka")
@Table("reslr")
@CompositePK({ "redate", "rerace", "rehorse" })
public class Races extends Model {

}
