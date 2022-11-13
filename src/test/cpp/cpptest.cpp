#include <bits/types/clock_t.h>
#include <verilated.h>
#include <verilated_fst_c.h>
#include <VUnrolledAES.h>

#include <array>
#include <cstdint>
#include <iostream>

void clock(VUnrolledAES *dut, VerilatedFstC* tfp){
  static uint time_counter = 0;
  dut->eval();
  tfp->dump(1000*time_counter);
  time_counter++;
  dut->clock = !dut->clock;
  dut->eval();
  tfp->dump(1000*time_counter);
  time_counter++;
  dut->clock = !dut->clock;
}


int main(int argc, char** argv) {
	Verilated::commandArgs(argc, argv);
	VUnrolledAES *dut = new VUnrolledAES();
	Verilated::traceEverOn(true);
	VerilatedFstC* tfp = new VerilatedFstC;
	dut->trace(tfp, 100);  // Trace 100 levels of hierarchy
	tfp->open("simx.fst");
	constexpr int Nk = 4;
	// e0370734313198a2885a308d3243f6a8
	constexpr std::array<uint32_t,4>input_text = {0x3243f6a8, 0x885a308d, 0x313198a2, 0xe0370734};
	// 0c0d0e0f08090a0b0405060700010203
	constexpr std::array<uint32_t,Nk>key = {0x00010203, 0x04050607, 0x08090a0b, 0x0c0d0e0f};
	constexpr std::array<uint32_t,4>cipher = {0x0faa45e6, 0x48c99144, 0x14ddece7, 0x92c16fd8};
	dut->clock = 0;
	dut->reset = 0;
	dut->io_AES_mode = 2;
	for(int i = 0; i < 4; i++) dut->io_input_text[i] = input_text[i];
	for(int i = 0; i < Nk; i++) dut->io_key[i] = key[i];
	clock(dut,tfp);
	for(int i = 0; i < 4; i++) std::cout<<std::hex<<dut->io_output_text[i]<<std::endl;
	for(int i = 0; i < 4; i++)dut->io_input_text[i] = dut->io_output_text[i];
	// for(int i = 0; i < 4; i++)assert(dut->io_output_text[i] == cipher[i]);
	std::cout<<"Enc done"<<std::endl;
	dut->io_AES_mode = 3;
	clock(dut,tfp);
	dut->final();
    tfp->close();
	for(int i = 0; i < 4; i++) std::cout<<std::hex<<dut->io_output_text[i]<<std::endl;
	for(int i = 0; i < 4; i++)assert(dut->io_output_text[i] == input_text[i]);

}
