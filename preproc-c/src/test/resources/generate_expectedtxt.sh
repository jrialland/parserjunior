#/bin/bash
set -euo pipefail

for f in $(find ./ -name "*.txt" ! -name "*.expected.txt"); do
    expectedf=$(echo $f|sed -e s/\.txt/.expected.txt/)
    cpp $f | grep -v '#' > $expectedf
done
