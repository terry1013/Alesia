// SLE Consultores
// Ejemplo de script para conversión de datos. 
// version: 2.0
//----------------------------------------------------------
import plugin.dbtweezer.SleUtilities

// Variables para rutina de redondeo
int iScale = 2
int iRoundDec = 1
int iTopScale = 5
int divisor = 1000

// Ejecuta division y redondea de acuerdo a las variables temporales. Ej:
// monto = SleUtilities.divideAndRoundDecimal(monto, divisor, iScale, iRoundDec, iTopScale)

${divideAndRoundDecimal}

// procesar
return true