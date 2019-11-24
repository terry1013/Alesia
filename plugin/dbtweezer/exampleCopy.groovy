// SLE Consultores
// Ejemplo de script para conversión de datos. 
// version: 1.3
//----------------------------------------------------------
import plugin.dbtweezer.SleUtilities

//variables de la tabla de base de datos
${record}

// Variables temporales
int iScale = 2
int iRoundDec = 1
int iTopScale = 5
int divisor = 1000

// Solo redondeo de acuerdo a las variables temporales
//iAmount = SleUtilities.roundDecimal(iAmount, iScale, iRoundDec, iTopScale)

// Ejecuta division y redondea de acuerdo a las variables temporales
iAmount = SleUtilities.divideAndRoundDecimal(iAmount, divisor, iScale, iRoundDec, iTopScale)

// imprime mensaje en anotaciones (Para depuraciones de código)
//SleUtilities.print("Valor de campo" + iAmount)

// retorno a ScriptEngine: 
// - retorne true para inicar aceptar cambios dentro de este script
// - Retorne flase para ignorar cambios

return true