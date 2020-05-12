package core.datasource.model;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("AlesiaDatabase")
@Table("GAMEHISTORIES")
@CompositePK({ "TIME", "TABLEPARAMS", "NAME" })
public class GamesHistory extends Model {

}
