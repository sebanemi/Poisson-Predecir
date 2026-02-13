⚽ Football Match Prediction System
📌 Versión 2.0 — Modelo Poisson Normalizado

**🧠 Descripción General**

Este proyecto implementa un sistema de predicción de partidos de fútbol utilizando datos históricos de la Premier League.

En esta versión se incorpora un modelo probabilístico basado en la Distribución de Poisson independiente, utilizando normalización por promedio de liga para mejorar la coherencia estadística del modelo.

El sistema ahora permite:

    -> Calcular goles esperados (λ) normalizados

    -> Generar matriz de probabilidades de marcador (0–5)

    -> Calcular probabilidades 1X2
    
    -> Obtener odds teóricas (fair odds)

**🎯 Objetivos de la Versión 2.0**

    -> Mantener arquitectura modular y escalable.
    
    -> Incorporar promedio de liga (μ) para normalización.
    
    -> Implementar modelo Poisson completo.
    
    -> Calcular probabilidades de resultado (1X2).
    
    -> Mostrar matriz de resultados exactos.
    
    -> Generar cuotas teóricas sin margen.

**📂 Fuente de Datos**

    Liga: Premier League
    
    Formato: CSV
    
    Columnas utilizadas (0-based):

Índice	Contenido 

* 3	Equipo Local
* 4	Equipo Visitante
* 5	Goles Local
* 6	Goles Visitante

El sistema ignora filas inválidas o incompletas.

**🏗 Arquitectura del Proyecto**

El sistema sigue un diseño modular con separación clara de responsabilidades.

📘 1️⃣ MatchReader

Responsabilidad:

* Leer el CSV línea por línea.
* Validar datos.
* Crear equipos dinámicamente.
* Actualizar estadísticas de equipo.
* Actualizar estadísticas globales de liga.

No realiza cálculos probabilísticos.

📊 2️⃣ TeamStats

Almacena estadísticas separadas por condición.

**Como Local**

* homeGoalsFor
* homeGoalsAgainst
* homeMatches

**Como Visitante**

* awayGoalsFor
* awayGoalsAgainst
* awayMatches

**Métodos principales**

    addHomeMatch()
    
    addAwayMatch()
    
    getHomeAverageGoalsFor()
    
    getHomeAverageGoalsAgainst()
    
    getAwayAverageGoalsFor()
    
    getAwayAverageGoalsAgainst()

🌍 3️⃣ LeagueStats

Nueva incorporación en esta versión.

Responsabilidad:

Calcular el promedio de goles por equipo en la liga (μ).

Fórmula

    μ = Total goles en liga / (Partidos × 2)

Este valor se utiliza para normalizar λ y evitar sesgos estructurales de la liga.

🧮 4️⃣ PoissonCalculator

Implementa el modelo probabilístico.

Funcionalidades

* Cálculo de probabilidad Poisson: 
    P(X = k) = (e^(-λ) × λ^k) / k!
* Generación de matriz de marcador 0–5.
* Cálculo de probabilidades 1X2.
* Impresión formateada de tabla de resultados.
* Conversión de probabilidades a odds teóricas.

🚀 5️⃣ Main

Coordina el flujo completo:

1. Inicializa estructuras.
2. Lee el CSV.
3. Calcula promedio de liga.
4. Calcula λ normalizado.
5. Genera matriz de probabilidades.
6. Calcula 1X2.
7. Muestra odds teóricas.

**📊 Modelo Estadístico Implementado**

🔹 Cálculo de λ normalizado

    λ_local =
    (Promedio goles local como local ×
    Promedio goles recibidos visitante como visitante)
    / μ
    
    λ_visitante =
    (Promedio goles visitante como visitante ×
    Promedio goles recibidos local como local)
    / μ

🔹 Matriz de Resultados

Para cada combinación i, j (0–5 goles):

    P(i,j) = P_local(i) × P_visitante(j)

Se genera una matriz de 36 combinaciones posibles.

🔹 Cálculo 1X2

* Local gana → i > j
* Empate → i = j
* Visitante gana → i < j

Las probabilidades se obtienen sumando las celdas correspondientes.

🔹 Odds Teóricas

Fair odds:

Odds = 1 / Probabilidad

No incluyen margen de bookmaker.

**🧠 Fundamentación Matemática**

✔ Uso de Poisson

* Los goles en fútbol:
* Son eventos discretos.
* Ocurren con baja frecuencia.
* Pueden modelarse como proceso Poisson bajo independencia.

✔ Normalización por μ

Dividir por el promedio de liga:
* Elimina sesgos estructurales.
* Ajusta diferencias entre temporadas.
* Mejora estabilidad del modelo.

✔ Separación local / visitante

* La localía impacta significativamente:
* Producción ofensiva
* Solidez defensiva

Separar estadísticas mejora precisión del λ estimado.

**📈 Estado Actual del Proyecto**

Funcionalidad	Estado

Lectura CSV	✅ Implementado

Validación de datos	✅ Implementado

Promedios local/visitante	✅ Implementado

Promedio de liga (μ)	✅ Implementado

λ normalizado	✅ Implementado

Matriz Poisson 0–5	✅ Implementado

Probabilidades 1X2	✅ Implementado

Odds teóricas	✅ Implementado

Backtesting	🔄 Pendiente

🔮 Próximas Etapas

* Over/Under 2.5
* Ambos Marcan (BTTS)
* Ajuste Dixon–Coles para empates
* Backtesting histórico automático
* Evaluación con:
  * Accuracy
  * Log Loss
  * ROI simulado
  * Comparación contra mercado real

**🏁 Conclusión**

La versión 2.0 transforma el sistema en un modelo probabilístico formal basado en fundamentos estadísticos sólidos.

La arquitectura modular permite escalar hacia modelos más complejos sin romper la estructura actual.

El proyecto ya cuenta con:

* Base matemática consistente
* Separación clara de responsabilidades
* Motor probabilístico funcional
* Generación de cuotas teóricas

Se encuentra listo para evolucionar hacia validación histórica y optimización avanzada.