letters =  ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z']
size = 300000000
frequency = {}
File.open('test-files/big.in', 'w') do |f|
  size.times do
    char = letters.sample
    frequency[char] ||= 0
    frequency[char] += 1
    f << char
  end
end

File.open('test-files/big.out', 'w') do |f|
  frequency.keys.sort.each do |char|
    f << "#{char} #{frequency[char]}\n"
  end
end
