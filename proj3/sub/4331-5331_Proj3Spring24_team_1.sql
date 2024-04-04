set echo on
SPOOL /tmp/log.txt

-- Get the table with the Title, Rating, Release Year and Genre List of the top 5 rated movies with at least
-- 150,000 votes, of the genres Adventure and Drama at least, and from the time period from 1991 to 2000.
Select PRIMARYTITLE, AVERAGERATING, STARTYEAR, GENRES 
FROM imdb00.TITLE_BASICS T join imdb00.TITLE_RATINGS R on T.TCONST = R.TCONST 
WHERE R.NUMVOTES >= 150000 
AND T.GENRES LIKE '%Adventure%Drama%' 
AND T.TITLETYPE LIKE '%ovie' 
AND T.STARTYEAR BETWEEN 1991 AND 2000 
ORDER BY R.AVERAGERATING DESC 
FETCH FIRST 5 ROWS ONLY;

-- Output:

-- PRIMARYTITLE                   | AVERAGERATING | STARTYEAR | GENRES
-- ---------------------------------------------------------------------------------------
-- Gladiator                      | 8.5           | 2000      | Action,Adventure,Drama
-- The Lion King                  | 8.5           | 1994      | Adventure,Animation,Drama
-- Almost Famous                  | 7.9           | 2000      | Adventure,Comedy,Drama
-- Crouching Tiger, Hidden Dragon | 7.9           | 2000      | Action,Adventure,Drama
-- Cast Away                      | 7.8           | 2000      | Adventure,Drama,Romance


-- Store the explaination of the plan for the given query in a known table.
EXPLAIN PLAN FOR (
SELECT PRIMARYTITLE, AVERAGERATING, STARTYEAR, GENRES 
FROM imdb00.TITLE_BASICS T JOIN imdb00.TITLE_RATINGS R ON T.TCONST = R.TCONST 
WHERE R.NUMVOTES >= 150000 
AND T.GENRES LIKE '%Adventure%Drama%' 
AND T.TITLETYPE LIKE '%ovie' 
AND T.STARTYEAR BETWEEN 1991 AND 2000 
ORDER BY R.AVERAGERATING DESC 
FETCH FIRST 5 ROWS ONLY
);

-- Output:

-- Explained.


-- Get the contents of the table populated by the EXPLAIN query.
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);

-- Output:

-- Plan hash value: 2653010624
-- ------------------------------------------------------------------------------------------------
-- | Id  | Operation                      | Name          | Rows  | Bytes | Cost (%CPU)| Time     |
-- ------------------------------------------------------------------------------------------------
-- |   0 | SELECT STATEMENT               |               |     5 | 10250 |    3846 (1)| 00:00:01 |
-- |*  1 |  VIEW                          |               |     5 | 10250 |    3846 (1)| 00:00:01 |
-- |*  2 |   WINDOW SORT PUSHED RANK      |               |    57 |  6555 |    3846 (1)| 00:00:01 |
-- |   3 |    NESTED LOOPS                |               |    57 |  6555 |    3845 (1)| 00:00:01 |
-- |   4 |     NESTED LOOPS               |               |  1380 |  6555 |    3845 (1)| 00:00:01 |
-- |*  5 |      TABLE ACCESS FULL         | TITLE_RATINGS |  1380 | 23460 |    1084 (2)| 00:00:01 |
-- |*  6 |      INDEX UNIQUE SCAN         | SYS_C00547784 |     1 |       |       1 (0)| 00:00:01 |
-- |*  7 |     TABLE ACCESS BY INDEX ROWID| TITLE_BASICS  |     1 |    98 |       2 (0)| 00:00:01 |
-- ------------------------------------------------------------------------------------------------
-- Predicate Information (identified by operation id):
-- 
-- ---------------------------------------------------
--    1 - filter("from$_subquery$_004"."rowlimit_$$_rownumber"<=5)
--    2 - filter(ROW_NUMBER() OVER ( ORDER BY INTERNAL_FUNCTION("R"."AVERAGERATING") DESC )<=5)
--    5 - filter("R"."NUMVOTES">=150000)
--    6 - access("T"."TCONST"="R"."TCONST")
--    7 - filter("T"."GENRES" LIKE U'%Adventure%Drama%' AND "T"."TITLETYPE" LIKE U'%ovie'
--               AND TO_NUMBER("T"."STARTYEAR")>=1991 AND TO_NUMBER("T"."STARTYEAR")<=2000 AND
--               "T"."GENRES" IS NOT NULL AND "T"."TITLETYPE" IS NOT NULL)

SPOOL OFF