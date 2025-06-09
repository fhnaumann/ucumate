# xml tests

Benchmark                                                (ucumateCaching)  Mode  Cnt  Score   Error  Units
BenchmarkFunctionalXMLTests.benchmarkUcumJavaConversion           disable  avgt    4  9,255 ± 0,218  ms/op
BenchmarkFunctionalXMLTests.benchmarkUcumJavaValidation           disable  avgt    4  0,551 ± 0,027  ms/op
BenchmarkFunctionalXMLTests.benchmarkUcumateConversion            disable  avgt    4  0,344 ± 0,021  ms/op
BenchmarkFunctionalXMLTests.benchmarkUcumateValidation            disable  avgt    4  1,643 ± 0,046  ms/op

Benchmark                                                (ucumateCaching)  Mode  Cnt  Score   Error  Units
BenchmarkFunctionalXMLTests.benchmarkUcumJavaConversion            enable  avgt    4  9,617 ± 0,430  ms/op
BenchmarkFunctionalXMLTests.benchmarkUcumJavaValidation            enable  avgt    4  0,498 ± 0,015  ms/op
BenchmarkFunctionalXMLTests.benchmarkUcumateConversion             enable  avgt    4  0,091 ± 0,004  ms/op
BenchmarkFunctionalXMLTests.benchmarkUcumateValidation             enable  avgt    4  0,016 ± 0,004  ms/op

Benchmark                                                 (ucumateCaching)  Mode  Cnt  Score   Error  Units
BenchmarkFunctionalXMLTests.benchmarkUcumJavaConversion  enableWithPreHeat  avgt    4  9,386 ± 1,577  ms/op
BenchmarkFunctionalXMLTests.benchmarkUcumJavaValidation  enableWithPreHeat  avgt    4  0,498 ± 0,639  ms/op
BenchmarkFunctionalXMLTests.benchmarkUcumateConversion   enableWithPreHeat  avgt    4  0,094 ± 0,011  ms/op
BenchmarkFunctionalXMLTests.benchmarkUcumateValidation   enableWithPreHeat  avgt    4  0,016 ± 0,003  ms/op

# json tests

Benchmark                                                       (ucumateCaching)  Mode  Cnt   Score   Error  Units
BenchmarkFunctionalJSONTests.benchmarkUcumJavaCommensurability           disable  avgt    4  35,724 ± 1,011  ms/op OLD
BenchmarkFunctionalJSONTests.benchmarkUcumJavaValidation                 disable  avgt    4   1,292 ± 0,096  ms/op OLD
BenchmarkFunctionalJSONTests.benchmarkUcumateCommensurability            disable  avgt    4   0,958 ± 0,032  ms/op NEW
BenchmarkFunctionalJSONTests.benchmarkUcumateValidation                  disable  avgt    4   7,115 ± 0,259  ms/op NEW
BenchmarkFunctionalJSONTests.benchmarkucumJavaConversion                 disable  avgt    4  26,195 ± 0,551  ms/op OLD
BenchmarkFunctionalJSONTests.benchmarkucumateConversion                  disable  avgt    4   1,087 ± 0,018  ms/op NEW

Benchmark                                                       (ucumateCaching)  Mode  Cnt   Score   Error  Units
BenchmarkFunctionalJSONTests.benchmarkUcumJavaCommensurability            enable  avgt    4  35,613 ± 1,575  ms/op
BenchmarkFunctionalJSONTests.benchmarkUcumJavaValidation                  enable  avgt    4   1,272 ± 0,042  ms/op
BenchmarkFunctionalJSONTests.benchmarkUcumateCommensurability             enable  avgt    4   0,240 ± 0,009  ms/op
BenchmarkFunctionalJSONTests.benchmarkUcumateConversion                   enable  avgt    4   0,274 ± 0,011  ms/op
BenchmarkFunctionalJSONTests.benchmarkUcumateValidation                   enable  avgt    4   0,030 ± 0,004  ms/op
BenchmarkFunctionalJSONTests.benchmarkucumJavaConversion                  enable  avgt    4  26,215 ± 0,373  ms/op

Benchmark                                                        (ucumateCaching)  Mode  Cnt   Score   Error  Units
BenchmarkFunctionalJSONTests.benchmarkUcumJavaCommensurability  enableWithPreHeat  avgt    4  35,835 ± 0,452  ms/op
BenchmarkFunctionalJSONTests.benchmarkUcumJavaValidation        enableWithPreHeat  avgt    4   1,322 ± 0,022  ms/op
BenchmarkFunctionalJSONTests.benchmarkUcumateCommensurability   enableWithPreHeat  avgt    4   0,256 ± 0,033  ms/op
BenchmarkFunctionalJSONTests.benchmarkUcumateConversion         enableWithPreHeat  avgt    4   0,280 ± 0,021  ms/op
BenchmarkFunctionalJSONTests.benchmarkUcumateValidation         enableWithPreHeat  avgt    4   0,030 ± 0,010  ms/op
BenchmarkFunctionalJSONTests.benchmarkucumJavaConversion        enableWithPreHeat  avgt    4  26,271 ± 0,227  ms/op