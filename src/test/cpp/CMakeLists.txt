add_executable(cpptest cpptest.cpp)
verilate(cpptest SOURCES ../../../UnrolledAES.v THREADS 4 TRACE_FST)