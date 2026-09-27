Git Project

Methods:
init
    creates ./git ./git/objects ./git/index ./git/HEAD
    does not overwrite already existing files

hashFile
    returns the sha1 hash of a given file
    uses MessageDigest.digest to hash byte array

saveBlob
    uses hashFile to get the hash of a given file
    creates file under in ./git/objects with hash as filename
    saves original file data with gzip compression to that file

    compress, compressFile, decompress, decompressFile
    compress & decompress are private helper methods to act on byte array
    compressFile & decompressFile are helper methods to call compress & decompress on a given file path

updateIndex
    adds given file as entry to ./git/index
    saves file hash and file path with respect to project folder

