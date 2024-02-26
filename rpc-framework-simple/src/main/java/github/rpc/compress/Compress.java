package github.rpc.compress;

import github.rpc.extension.SPI;



@SPI
public interface Compress {

    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);
}
