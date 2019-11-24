// SLE Consultores
// Ejemplo de script para conversión de datos. 
// version: 1.3
//----------------------------------------------------------
import plugin.dbtweezer.SleUtilities

// Variables temporales
int iScale = 2
int iRoundDec = 1
int iTopScale = 5
int divisor = 1000

// Ejecuta division y redondea de acuerdo a las variables temporales
${COLUMN_NAME} = SleUtilities.divideAndRoundDecimal(${COLUMN_NAME}, divisor, iScale, iRoundDec, iTopScale)

// retorno a ScriptEngine: 
// - retorne true para inicar aceptar cambios dentro de este script
// - Retorne flase para ignorar cambios

return true